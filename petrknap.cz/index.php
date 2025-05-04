<?php

declare(strict_types=1);

require_once __DIR__ . '/vendor/autoload.php';

(new PetrKnap\Website\ReverseProxy(
    new GuzzleHttp\Client([
        'base_uri' => 'https://petrknap.github.io/',
        'handler' => new GuzzleHttp\Handler\CurlHandler(),
    ]),
    new Symfony\Component\Cache\Adapter\PhpFilesAdapter('reverse_proxy', 24 * 60 * 60, __DIR__ . '/cache'),
    [
        '/index_cz.html',
        '/sitemap.xml',
        '/robots.txt',
    ],
    '/404.html',
))->forward(empty($_GET['uri'] ?? '') ? '/index_cz.html' : "/{$_GET['uri']}");
