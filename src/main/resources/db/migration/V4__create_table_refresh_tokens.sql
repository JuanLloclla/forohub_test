create table refresh_tokens(
    id bigint not null auto_increment,
    token varchar(255) not null unique,
    usuario_id bigint not null,
    expiracion datetime not null,

    primary key(id),
    constraint fk_refresh_tokens_usuario foreign key(usuario_id) references usuarios(id)
    ON DELETE CASCADE -- Si eliminas el usuario, sus refresh tokens se eliminan automáticamente
);