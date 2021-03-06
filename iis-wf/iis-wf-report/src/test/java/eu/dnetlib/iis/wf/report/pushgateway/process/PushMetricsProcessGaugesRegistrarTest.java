package eu.dnetlib.iis.wf.report.pushgateway.process;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PushMetricsProcessGaugesRegistrarTest {

    @Test
    public void shouldRegisterToEmptyOnError() {
        // given
        PushMetricsProcess.GaugesRegistrar gaugesRegistrar = new PushMetricsProcess.GaugesRegistrar();

        // when
        Optional<CollectorRegistry> result = gaugesRegistrar.register(() -> null);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldRegisterToEmptyOnRegistrationFailure() {
        // given
        PushMetricsProcess.GaugesRegistrar gaugesRegistrar = new PushMetricsProcess.GaugesRegistrar();
        CollectorRegistry registry = mock(CollectorRegistry.class);
        doThrow(RuntimeException.class).when(registry).register(any());

        // when
        Optional<CollectorRegistry> result = gaugesRegistrar.register(Collections.singletonList(mock(Gauge.class)), () -> registry);

        // then
        assertFalse(result.isPresent());
        verify(registry, times(1)).register(any());
    }

    @Test
    public void shouldRegisterToNonEmpty() {
        // given
        PushMetricsProcess.GaugesRegistrar gaugesRegistrar = new PushMetricsProcess.GaugesRegistrar();
        Gauge gauge = mock(Gauge.class);
        CollectorRegistry collectorRegistry = mock(CollectorRegistry.class);

        // when
        Optional<CollectorRegistry> result = gaugesRegistrar.register(Collections.singletonList(gauge), () -> collectorRegistry);

        // then
        assertTrue(result.isPresent());
        assertEquals(collectorRegistry, result.get());
        verify(collectorRegistry, times(1)).register(gauge);
    }

}
