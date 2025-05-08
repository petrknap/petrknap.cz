<?php

declare(strict_types=1);

namespace PetrKnap\Website;

use GuzzleHttp\Client;
use GuzzleHttp\Exception\ClientException;
use Symfony\Contracts\Cache\CacheInterface;

final class ReverseProxy {
    public const FORWARDED_HEADERS = [
        'content-type',
    ];

    private $client;
    private $cache;
    private $allowedUris;
    private $error404Uri;

    public function __construct(
        Client $client,
        CacheInterface $cache,
        array $knownUris,
        string $error404Uri
    ) {
        $this->client = $client;
        $this->cache = $cache;
        $this->allowedUris = $knownUris;
        $this->error404Uri = $error404Uri;
    }

    public function forward(string $uri): void {
        if (in_array($uri, $this->allowedUris)) {
            $this->getResponse($uri, false)->send();
        } else {
            $this->getResponse($this->error404Uri, true)->send();
        }
    }

    private function getResponse(string $uri, bool $allowFailure): ReverseProxyResponse {
        $cacheItem = $this->cache->getItem(sha1($uri));
        if ($cacheItem->isHit()) {
            return $cacheItem->get();
        }
        try {
            $clientResponse = $this->client->request('GET', $uri);
        } catch (ClientException $clientException) {
            if ($allowFailure) {
                $clientResponse = $clientException->getResponse();
            } else {
                throw $clientException;
            }
        }
        $response = ReverseProxyResponse::create($clientResponse);
        $cacheItem->set($response);
        $this->cache->save($cacheItem);
        return $response;
    }
}
