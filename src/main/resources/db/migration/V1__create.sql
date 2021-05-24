CREATE TABLE users_settings(
        chat_id NUMERIC NOT NULL UNIQUE,
        line_delimiter VARCHAR NOT NULL,
        in_row_delimiter VARCHAR NOT NULL,
        name_col NUMERIC,
        addr_col NUMERIC NOT NULL,
        info_col NUMERIC,
        PRIMARY KEY (chat_id));