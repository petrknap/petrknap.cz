FROM php:7.3-apache

WORKDIR /var/www/html
RUN a2enmod rewrite

# region included composer
# hadolint ignore=DL3008
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      git \
      unzip \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/* \
;
COPY --from=composer:2 /usr/bin/composer /usr/local/bin/composer
# endregion
