package foo.bar;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import lombok.SneakyThrows;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import static foo.bar.ByteUtils.bytesToLong;
import static foo.bar.ByteUtils.longToBytes;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;

public class Main {
    private static final String CHANNEL = "metrics";

    public static void main(String... args) throws IOException, InterruptedException {
        final Connection nc = Nats.connect("nats://localhost:4222");
        final MetricSynchronizer ms = new MetricSynchronizer(nc);
        new Thread(new AvailabilityCallsRunner(nc)).start();
        new Thread(ms).start();
        while (true) {
            Thread.sleep(1_000);
            System.out.format("%s%n", ms.getMetrics());
        }
    }

    private static class AvailabilityCallsRunner implements Runnable {
        private final Connection c;

        public AvailabilityCallsRunner(Connection c) {
            this.c = c;
        }

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                c.publish(CHANNEL, longToBytes(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
                Thread.sleep(ThreadLocalRandom.current().nextInt(200));
            }
        }
    }

    private static class MetricSynchronizer implements Runnable {
        private final Connection c;
        private final HashMap<Long, Long> xs = new HashMap<>();

        public MetricSynchronizer(Connection c) {
            this.c = c;
        }

        @SneakyThrows
        @Override
        public void run() {
            final Subscription sub = c.subscribe(CHANNEL);
            while (true)
                xs.merge(bytesToLong(sub.nextMessage(Duration.ofDays(365)).getData()), 1L, Long::sum);
        }

        public String getMetrics() {
            // last n truncated 20 seconds
            return xs.entrySet().stream()
                    .collect(groupingBy(e -> e.getKey() / 20, summingLong(e -> e.getValue())))
                    .entrySet().stream().sorted((a, b) -> Long.compare(b.getKey(), a.getKey())).limit(10).map(e -> e.getValue().toString())
                    .reduce((a, b) -> a + ", " + b).orElse("");
        }
    }
}
