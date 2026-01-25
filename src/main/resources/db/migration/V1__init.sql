CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	email TEXT NOT NULL UNIQUE,
	password_hash TEXT NOT NULL,
	display_name TEXT NOT NULL,
	platform_role TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE events (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	title TEXT NOT NULL,
	description TEXT NOT NULL,
	city TEXT NOT NULL,
	venue_name TEXT NOT NULL,
	venue_address TEXT NOT NULL,
	starts_at TIMESTAMPTZ NOT NULL,
	ends_at TIMESTAMPTZ NOT NULL,
	status TEXT NOT NULL,
	created_by UUID NOT NULL REFERENCES users(id),
	image_file_id UUID NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_memberships (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
	user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	role TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	UNIQUE(event_id, user_id)
);

CREATE TABLE seat_inventories (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
	category TEXT NOT NULL,
	price_per_seat_paise BIGINT NOT NULL,
	total_count INT NOT NULL,
	available_count INT NOT NULL,
	version BIGINT NOT NULL DEFAULT 0,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	UNIQUE(event_id, category)
);

CREATE TABLE bookings (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	event_id UUID NOT NULL REFERENCES events(id),
	user_id UUID NOT NULL REFERENCES users(id),
	seat_category TEXT NOT NULL,
	quantity INT NOT NULL,
	total_amount_paise BIGINT NOT NULL,
	currency TEXT NOT NULL,
	status TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
	provider TEXT NOT NULL,
	provider_order_id TEXT NOT NULL,
	provider_payment_id TEXT NULL,
	amount_paise BIGINT NOT NULL,
	currency TEXT NOT NULL,
	status TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE stored_files (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	original_filename TEXT NOT NULL,
	content_type TEXT NOT NULL,
	size_bytes BIGINT NOT NULL,
	storage_path TEXT NOT NULL,
	uploaded_by UUID NOT NULL REFERENCES users(id),
	uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_events_city_starts_at ON events(city, starts_at);
CREATE INDEX idx_bookings_event_id ON bookings(event_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_seat_inventories_event_id ON seat_inventories(event_id);
