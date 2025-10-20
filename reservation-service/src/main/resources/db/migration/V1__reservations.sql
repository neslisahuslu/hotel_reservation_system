CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE IF NOT EXISTS reservations
(
    id               UUID PRIMARY KEY,
    hotel_id         UUID NOT NULL,
    room_id          UUID NOT NULL,
    user_id          UUID NOT NULL,
    guest_name       VARCHAR(255) NOT NULL,
    check_in_date    DATE NOT NULL,
    check_out_date   DATE NOT NULL,
    created_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_date     TIMESTAMP WITHOUT TIME ZONE,
    version          INTEGER NOT NULL,

    CONSTRAINT reservations_dates_ck
        CHECK (check_out_date > check_in_date),

    CONSTRAINT reservations_room_date_excl
        EXCLUDE USING gist (
        room_id WITH =,
        daterange(check_in_date, check_out_date, '[)') WITH &&
        )
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reservations_room_exact
    ON reservations (room_id, check_in_date, check_out_date);

CREATE INDEX IF NOT EXISTS idx_reservations_room_checkin
    ON reservations (room_id, check_in_date);
