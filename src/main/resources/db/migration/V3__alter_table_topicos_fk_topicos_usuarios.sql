alter table topicos add constraint fk_topico_usuario
foreign key (usuario_id) references usuarios(id);