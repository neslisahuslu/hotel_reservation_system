CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================
-- HOTELS
-- =========================
CREATE TABLE IF NOT EXISTS hotels (
                                      id              UUID PRIMARY KEY,
                                      created_date    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_date    TIMESTAMPTZ,
                                      version         INTEGER NOT NULL DEFAULT 0,

                                      name            VARCHAR(200) NOT NULL,
                                      address         VARCHAR(1000),
                                      star_rating     INTEGER,

                                      CONSTRAINT chk_hotels_star_rating
                                          CHECK (star_rating IS NULL OR (star_rating >= 1 AND star_rating <= 5))
);

-- =========================
-- ROOMS
-- =========================
CREATE TABLE IF NOT EXISTS rooms (
                                     id               UUID PRIMARY KEY,
                                     created_date     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     updated_date     TIMESTAMPTZ,
                                     version          INTEGER NOT NULL DEFAULT 0,

                                     hotel_id         UUID NOT NULL,
                                     room_number      VARCHAR(50)  NOT NULL,
                                     capacity         INTEGER      NOT NULL,
                                     price_per_night  NUMERIC(12,2) NOT NULL,

                                     CONSTRAINT fk_rooms_hotel
                                         FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE,

                                     CONSTRAINT uq_hotel_room UNIQUE (hotel_id, room_number),

                                     CONSTRAINT chk_rooms_capacity
                                         CHECK (capacity >= 1),
                                     CONSTRAINT chk_rooms_price_nonnegative
                                         CHECK (price_per_night >= 0)
);

-- =========================
-- İNDEKSLER
-- =========================
-- Room -> Hotel aramaları için
CREATE INDEX IF NOT EXISTS idx_room_hotel_id ON rooms (hotel_id);
