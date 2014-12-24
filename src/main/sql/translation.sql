CREATE TABLE translation (
    id integer DEFAULT nextval(('seq_translation'::text)::regclass) NOT NULL,
    timestamp text,
    ebms_conversation_id text,
    ebms_message_id text,
    ebms_ref_to_message_id text,
    ws_message_id text,
    ws_relates_to text
);

CREATE SEQUENCE seq_translation
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1
    CYCLE;

ALTER TABLE ONLY translation
    ADD CONSTRAINT translation_pkey PRIMARY KEY (id);
