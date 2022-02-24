CREATE TABLE IF NOT EXISTS `imgur_tag` (
    `id` BIGINT(18) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL,
    `guild_id` BIGINT(18) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `fk_imgur_tag_guild_idx` (`guild_id` ASC) VISIBLE,
    CONSTRAINT `fk_imgur_tag_guild`
        FOREIGN KEY (`guild_id`)
            REFERENCES `guild` (`guild_id`)
            ON DELETE CASCADE
    );