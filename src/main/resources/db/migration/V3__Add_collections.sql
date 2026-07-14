-- Таблица коллекций
CREATE TABLE collections (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Индекс для ускорения выборки всех коллекций конкретного пользователя
CREATE INDEX idx_collections_user_id ON collections(user_id);

-- Таблица связи коллекций и фильмов
CREATE TABLE collection_movies (
    collection_id BIGINT NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, movie_id)
);