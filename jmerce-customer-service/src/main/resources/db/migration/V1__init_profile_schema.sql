CREATE TABLE profile.customer
(
    id           uuid        NOT NULL DEFAULT uuidv7(),
    user_id      uuid        NOT NULL,
    given_name   text        NOT NULL,
    family_name  text        NOT NULL,
    phone_number text,
    status       text        NOT NULL DEFAULT 'ACTIVE',
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_customer
        PRIMARY KEY (id),
    CONSTRAINT uk_customer_identity
        UNIQUE (user_id),
    CONSTRAINT ck_customer_given_name
        CHECK (
            given_name = btrim(given_name)
                AND length(given_name) BETWEEN 1 AND 100
            ),
    CONSTRAINT ck_customer_family_name
        CHECK (
            family_name = btrim(family_name)
                AND length(family_name) BETWEEN 1 AND 100
            ),
    CONSTRAINT ck_customer_phone_number
        CHECK (
            phone_number IS NULL
                OR phone_number ~ '^\+[1-9][0-9]{7,14}$'
            ),
    CONSTRAINT ck_customer_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT ck_customer_timestamps
        CHECK (updated_at >= created_at)
);

CREATE TABLE profile.address
(
    id             uuid        NOT NULL DEFAULT uuidv7(),
    customer_id    uuid        NOT NULL,
    purpose        text        NOT NULL,
    recipient_name text        NOT NULL,
    line_1         text        NOT NULL,
    line_2         text,
    city           text        NOT NULL,
    region         text,
    postal_code    text,
    country_code   text        NOT NULL,
    phone_number   text,
    is_default     boolean     NOT NULL DEFAULT false,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_address
        PRIMARY KEY (id),
    CONSTRAINT fk_address_customer
        FOREIGN KEY (customer_id)
            REFERENCES profile.customer
            ON UPDATE RESTRICT
            ON DELETE CASCADE,
    CONSTRAINT ck_address_purpose
        CHECK (purpose IN ('SHIPPING', 'BILLING')),
    CONSTRAINT ck_address_recipient_name
        CHECK (
            recipient_name = btrim(recipient_name)
                AND length(recipient_name) BETWEEN 1 AND 200
            ),
    CONSTRAINT ck_address_line_1
        CHECK (
            line_1 = btrim(line_1)
                AND length(line_1) BETWEEN 1 AND 300
            ),
    CONSTRAINT ck_address_line_2
        CHECK (
            line_2 IS NULL
                OR (
                line_2 = btrim(line_2)
                    AND length(line_2) BETWEEN 1 AND 300
                )
            ),
    CONSTRAINT ck_address_city
        CHECK (
            city = btrim(city)
                AND length(city) BETWEEN 1 AND 150
            ),
    CONSTRAINT ck_address_region
        CHECK (
            region IS NULL
                OR (
                region = btrim(region)
                    AND length(region) BETWEEN 1 AND 150
                )
            ),
    CONSTRAINT ck_address_postal_code
        CHECK (
            postal_code IS NULL
                OR (
                postal_code = btrim(postal_code)
                    AND length(postal_code) BETWEEN 1 AND 32
                )
            ),
    CONSTRAINT ck_address_country_code
        CHECK (country_code ~ '^[A-Z]{2}$'),
    CONSTRAINT ck_address_phone_number
        CHECK (
            phone_number IS NULL
                OR phone_number ~ '^\+[1-9][0-9]{7,14}$'
            ),
    CONSTRAINT ck_address_timestamps
        CHECK (updated_at >= created_at)
);

CREATE UNIQUE INDEX ux_address_default_purpose
    ON profile.address (customer_id, purpose)
    WHERE is_default;
