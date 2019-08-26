CREATE SEQUENCE queue_mapping_id_seq START 1;

CREATE SEQUENCE session_mapping_id_seq START 1;

CREATE SEQUENCE queue_predistribution_id_seq START 1;

CREATE SEQUENCE queue_saved_message_id_seq START 1;

create table queue_saved_message(id integer not null default nextval('queue_saved_message_id_seq'::regclass), message_id varchar(50) primary key not null default replace(upper(uuid_in(md5(random()::text || now()::text)::cstring)::text), '-', ''), customer_name varchar(150) not null default '', shop_id integer not null default 0, message jsonb not null default '{}', op_time timestamp with time zone not null default now());

Create Index idx_tb_queue_saved_message On queue_saved_message(customer_name, shop_id);

create table queue_predistribution(id integer not null default nextval('queue_predistribution_id_seq'::regclass), customer_name varchar(150) not null default '', shop_id integer not null default 0, product_id varchar(150) not null default '*', inqueue_time timestamp with time zone not null default now());

Create Unique Index idx_tb_queue_predistribution_UNQ On queue_predistribution(customer_name, shop_id, product_id);

create table queue_mapping(id integer not null default nextval('queue_mapping_id_seq'::regclass), customer_name varchar(150) not null default '', shop_id integer not null default 0, product_id varchar(150) not null default '*', session_id varchar(50) primary key not null default replace(upper(uuid_in(md5(random()::text || now()::text)::cstring)::text), '-', ''), seat_id integer not null default 0, seat_name varchar(150) not null default '',  status integer not null default 1, request_count integer not null default 1, distributed_time timestamp with time zone not null default to_timestamp(0), inqueue_time timestamp with time zone not null default now(), last_ack_time timestamp with time zone not null default now());

Create Unique Index idx_tb_queue_mapping_userid_shopid_UNQ On queue_mapping(customer_name, shop_id);

Create Index idx_tb_queue_mapping_last_ack_time_shop_id On queue_mapping(last_ack_time, shop_id);

create Unique INDEX idx_queue_saved_message_id_UNQ ON queue_saved_message(id);

create table session_mapping(id integer not null default nextval('session_mapping_id_seq'::regclass), customer_name varchar(150) not null default '', shop_id integer not null default 0, product_id varchar(150) not null default '*', session_id varchar(50) primary key not null, seat_id integer not null default 0, seat_name varchar(150) not null default '', status integer not null default 1, request_count integer not null default 1, distributed_time timestamp with time zone not null default to_timestamp(0), inqueue_time timestamp with time zone not null default now(), last_ack_time timestamp with time zone not null default now());

ALTER TABLE supplier ADD COLUMN assign_strategy SMALLINT DEFAULT '0';
COMMENT ON COLUMN supplier.assign_strategy is '分配策略，0：轮询，1：最闲优先，2：随机分配';

ALTER TABLE seat ADD COLUMN bind_wx SMALLINT DEFAULT '0';
COMMENT ON COLUMN seat.bind_wx is '绑定微信，0：未绑定，1：已绑定';