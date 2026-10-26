package com.thecreatorco.backendforge.common.correlation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void generatesSafeUuidWhenNoCorrelationIdProvided() throws ServletException, IOException {
        AtomicReference<String> mdcDuringExecution = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuringExecution.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        String correlationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(correlationId).isNotNull();
        assertThat(UUID.fromString(correlationId)).isNotNull();
        assertThat(mdcDuringExecution.get()).isEqualTo(correlationId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void preservesAndEchoesValidCorrelationId() throws ServletException, IOException {
        String validId = "client-trace-123_456.789";
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, validId);
        AtomicReference<String> mdcDuringExecution = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuringExecution.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(validId);
        assertThat(mdcDuringExecution.get()).isEqualTo(validId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void acceptsRequestIdFallbackAndEchoesBothHeaders() throws ServletException, IOException {
        String requestId = "req-987-xyz";
        request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, requestId);
        AtomicReference<String> mdcDuringExecution = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuringExecution.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(requestId);
        assertThat(response.getHeader(CorrelationIdFilter.REQUEST_ID_HEADER)).isEqualTo(requestId);
        assertThat(mdcDuringExecution.get()).isEqualTo(requestId);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void prefersCorrelationIdOverRequestIdWhenBothPresent() throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "preferred-corr-id");
        request.addHeader(CorrelationIdFilter.REQUEST_ID_HEADER, "ignored-req-id");
        AtomicReference<String> mdcDuringExecution = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuringExecution.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("preferred-corr-id");
        assertThat(mdcDuringExecution.get()).isEqualTo("preferred-corr-id");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "injected\r\nHeader: value",
            "injected\nNew-Line: value",
            "injected\rCarriage-Return: value",
            "has space inside",
            "semi;colon",
            "<script>alert(1)</script>",
            "quotes\"around\"",
            "tabs\tinside",
            "control\u0000null"
    })
    void rejectsMalformedOrInjectionCorrelationIds(String malformedId) throws ServletException, IOException {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, malformedId);
        AtomicReference<String> mdcDuringExecution = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuringExecution.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        String generated = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generated).isNotNull();
        assertThat(generated).isNotEqualTo(malformedId);
        assertThat(generated).doesNotContain("\r", "\n", "\t", " ", ";", "<", ">", "\"");
        assertThat(UUID.fromString(generated)).isNotNull();
        assertThat(mdcDuringExecution.get()).isEqualTo(generated);
    }

    @Test
    void rejectsExcessivelyLongCorrelationId() throws ServletException, IOException {
        String longId = "a".repeat(CorrelationIdFilter.MAX_CORRELATION_ID_LENGTH + 1);
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, longId);
        AtomicReference<String> mdcDuringExecution = new AtomicReference<>();
        FilterChain chain = (req, res) -> mdcDuringExecution.set(MDC.get(CorrelationIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        String generated = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(generated).isNotNull();
        assertThat(generated).isNotEqualTo(longId);
        assertThat(generated.length()).isLessThanOrEqualTo(CorrelationIdFilter.MAX_CORRELATION_ID_LENGTH);
        assertThat(UUID.fromString(generated)).isNotNull();
    }

    @Test
    void cleansUpMdcWhenFilterChainThrowsException() {
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "safe-id");
        FilterChain throwingChain = (req, res) -> {
            assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isEqualTo("safe-id");
            throw new RuntimeException("Simulated filter failure");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, throwingChain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Simulated filter failure");

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
