-- Уникальный индекс, гарантирующий, что у пользователя может быть только один активный отзыв на фильм.
CREATE UNIQUE INDEX idx_unique_active_review 
ON reviews (user_id, movie_id) 
WHERE is_deleted = false;