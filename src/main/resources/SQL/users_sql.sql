create table users(
                      user_id bigint primary key auto_increment,
                      email varchar(100) not null,
                      password varchar(255) not null,
                      username varchar(20) not null,
                      phone varchar(30) not null,
                      provider varchar(100),
                      provider_id bigint,
                      role varchar(20) not null,
                      status varchar(20) not null,
                      created_at datetime not null,
                      deleted_at datetime
);

create table user_image(
                           image_id bigint primary key auto_increment,
                           url varchar(255) not null,
                           user_id bigint not null
);

create table refresh_token(
                              refresh_id bigint primary key auto_increment,
                              user_id bigint not null,
                              refresh_token varchar(255) not null,
                              expiration varchar(255) not null
);