<?php

declare(strict_types=1);

namespace PetrKnap\Website;

use Psr\Http\Message\ResponseInterface;

final class ReverseProxyResponse {
    private $httpCode;
    private $headers;
    private $body;

    public function __construct(
        int $httpCode,
        array $headers,
        string $body
    ) {
        $this->httpCode = $httpCode;
        $this->headers = $headers;
        $this->body = $body;
    }

    public static function create(ResponseInterface $response): self {
        $headers = [];
        foreach ($response->getHeaders() as $headerKey => $headerValues) {
            if (in_array(strtolower($headerKey), ReverseProxy::FORWARDED_HEADERS)) {
                $headers[$headerKey] = $headerValues[0] ?? null;
            }
        }
        return new self($response->getStatusCode(), $headers, $response->getBody()->getContents());
    }

    public function send(): void {
        http_response_code($this->httpCode);
        foreach ($this->headers as $headerKey => $headerValue) {
            header($headerKey . ': ' . $headerValue);
        }
        echo $this->body;
    }
}
