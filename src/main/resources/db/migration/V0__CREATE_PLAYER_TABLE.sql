-- Pit Schema

-- Player Table
CREATE TABLE "${table_prefix}player_names" (
    "UUID"          ${uuidtype} NOT NULL,
    "NAME"          CHARACTER VARYING(16) NOT NULL,
    "LOWER_NAME"    CHARACTER VARYING(16) GENERATED ALWAYS AS (LOWER("NAME")),
    CONSTRAINT "${table_prefix}uuid_name_unique" UNIQUE ("UUID", "NAME")
)${options};

-- Stats Table
CREATE TABLE "${table_prefix}player_stats" (
    "UUID"                      ${uuidtype} NOT NULL,
    "BOW_DAMAGE_TAKEN"          FLOAT DEFAULT 0 NOT NULL,
    "DAMAGE_TAKEN"              FLOAT DEFAULT 0 NOT NULL,
    "DEATHS"                    INT DEFAULT 0 NOT NULL,
    "MELEE_DAMAGE_TAKEN"        FLOAT DEFAULT 0 NOT NULL,
    "BLOCKS_BROKEN"             INT DEFAULT 0 NOT NULL,
    "BLOCKS_PLACED"             INT DEFAULT 0 NOT NULL,
    "CHAT_MESSAGES"             INT DEFAULT 0 NOT NULL,
    "FISHING_RODS_LAUNCHED"     INT DEFAULT 0 NOT NULL,
    "GOLDEN_APPLES_EATEN"       INT DEFAULT 0 NOT NULL,
    "JUMPS_INTO_PIT"            INT DEFAULT 0 NOT NULL,
    "LAVA_BUCKETS_EMPTIED"      INT DEFAULT 0 NOT NULL,
    "LEFT_CLICKS"               INT DEFAULT 0 NOT NULL,
    "ARROW_HITS"                INT DEFAULT 0 NOT NULL,
    "ARROW_SHOTS"               INT DEFAULT 0 NOT NULL,
    "ASSISTS"                   INT DEFAULT 0 NOT NULL,
    "BOW_DAMAGE_DEALT"          FLOAT DEFAULT 0 NOT NULL,
    "DIAMOND_ITEMS_PURCHASED"   INT DEFAULT 0 NOT NULL,
    "LAUNCHES"                  INT DEFAULT 0 NOT NULL,
    "DAMAGE_DEALT"              FLOAT DEFAULT 0 NOT NULL,
    "HIGHEST_STREAK"            INT DEFAULT 0 NOT NULL,
    "KILLS"                     INT DEFAULT 0 NOT NULL,
    "MELEE_DAMAGE_DEALT"        FLOAT DEFAULT 0 NOT NULL,
    "SWORD_HITS"                INT DEFAULT 0 NOT NULL,
    "GOLD_EARNED"               INT DEFAULT 0 NOT NULL,
    "GOLDEN_HEADS_EATEN"        INT DEFAULT 0 NOT NULL,
    CONSTRAINT "${table_prefix}uuid_unique" UNIQUE ("UUID")
)${options};

-- Indexes
CREATE INDEX "${table_prefix}player_name_idx" ON "${table_prefix}player_names" ("NAME");
CREATE INDEX "${table_prefix}player_lower_name_idx" ON "${table_prefix}player_names" ("LOWER_NAME");