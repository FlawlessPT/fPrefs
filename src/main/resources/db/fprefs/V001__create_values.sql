CREATE TABLE IF NOT EXISTS fprefs_values (
    uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    pref_id VARCHAR(64) NOT NULL,
    value VARCHAR(64) NOT NULL,
    PRIMARY KEY (uuid, pref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
