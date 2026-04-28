create table topicos(
    id bigint not null auto_increment,
    titulo varchar(255) not null,
    mensaje varchar(255) not null,
    fecha datetime not null,
    status varchar(50) not null,
    usuario_id bigint not null,
    curso varchar(100) not null,

    primary key (id),
    unique (titulo, mensaje)
);