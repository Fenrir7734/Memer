CREATE TABLE IF NOT EXISTS `guild` (
    `guild_id` BIGINT(18) NOT NULL,
    `prefix` VARCHAR(3) NOT NULL,
    `nsfw` TINYINT NOT NULL,
    PRIMARY KEY (`guild_id`)
    );
