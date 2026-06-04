--liquibase formatted sql

--changeset monolith:001-modulith-event-publication-table
CREATE TABLE IF NOT EXISTS event_publication (
    id                     UUID NOT NULL,
    listener_id            TEXT NOT NULL,
    event_type             TEXT NOT NULL,
    serialized_event       TEXT NOT NULL,
    publication_date       TIMESTAMPTZ NOT NULL,
    completion_date        TIMESTAMPTZ,
    status                 TEXT,
    completion_attempts    INT,
    last_resubmission_date TIMESTAMPTZ,
    PRIMARY KEY (id)
);

--changeset monolith:001-modulith-event-publication-serialized-event-index
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx
    ON event_publication (md5(serialized_event));

--changeset monolith:001-modulith-event-publication-completion-date-index
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx
    ON event_publication (completion_date);
