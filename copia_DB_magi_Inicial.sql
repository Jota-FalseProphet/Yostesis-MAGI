--
-- PostgreSQL database dump
--

-- Dumped from database version 16.8 (Ubuntu 16.8-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.8 (Ubuntu 16.8-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ausencies; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.ausencies (
    id_ausencia integer NOT NULL,
    id_docent integer NOT NULL,
    fecha_ausencia date NOT NULL,
    is_full_day boolean DEFAULT false
);


ALTER TABLE public.ausencies OWNER TO magiuser;

--
-- Name: ausencies_id_ausencia_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.ausencies_id_ausencia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ausencies_id_ausencia_seq OWNER TO magiuser;

--
-- Name: ausencies_id_ausencia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.ausencies_id_ausencia_seq OWNED BY public.ausencies.id_ausencia;


--
-- Name: ausencies_sessio; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.ausencies_sessio (
    id_ausencia integer NOT NULL,
    id_sessio integer NOT NULL
);


ALTER TABLE public.ausencies_sessio OWNER TO magiuser;

--
-- Name: docent; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.docent (
    id_docent integer NOT NULL,
    document character varying(50),
    nom character varying(100),
    cognom1 character varying(100),
    cognom2 character varying(100),
    tipus_doc character varying(50),
    sexe character varying(10),
    data_ingres date,
    hores_lloc integer,
    hores_dedicades integer,
    data_naix date,
    ensenyament character varying(100),
    organisme character varying(100),
    username character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    rol character varying(50)
);


ALTER TABLE public.docent OWNER TO magiuser;

--
-- Name: docent_id_docent_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.docent_id_docent_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.docent_id_docent_seq OWNER TO magiuser;

--
-- Name: docent_id_docent_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.docent_id_docent_seq OWNED BY public.docent.id_docent;


--
-- Name: docent_sessio; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.docent_sessio (
    id_asignacion integer NOT NULL,
    id_docent integer NOT NULL,
    id_sessio integer NOT NULL,
    ocupacion character varying(100)
);


ALTER TABLE public.docent_sessio OWNER TO magiuser;

--
-- Name: docent_sessio_id_asignacion_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.docent_sessio_id_asignacion_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.docent_sessio_id_asignacion_seq OWNER TO magiuser;

--
-- Name: docent_sessio_id_asignacion_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.docent_sessio_id_asignacion_seq OWNED BY public.docent_sessio.id_asignacion;


--
-- Name: guardies; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.guardies (
    id_guardia integer NOT NULL,
    docent_assignat integer NOT NULL,
    docent_absent integer NOT NULL,
    id_sessio integer NOT NULL,
    fecha_guardia date NOT NULL
);


ALTER TABLE public.guardies OWNER TO magiuser;

--
-- Name: guardies_id_guardia_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.guardies_id_guardia_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.guardies_id_guardia_seq OWNER TO magiuser;

--
-- Name: guardies_id_guardia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.guardies_id_guardia_seq OWNED BY public.guardies.id_guardia;


--
-- Name: sessions_horari; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.sessions_horari (
    id_sessio integer NOT NULL,
    plantilla character varying(50),
    dia_setmana character varying(20),
    sessio_ordre integer,
    hora_desde time without time zone,
    hora_fins time without time zone
);


ALTER TABLE public.sessions_horari OWNER TO magiuser;

--
-- Name: sessions_horari_id_sessio_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.sessions_horari_id_sessio_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sessions_horari_id_sessio_seq OWNER TO magiuser;

--
-- Name: sessions_horari_id_sessio_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.sessions_horari_id_sessio_seq OWNED BY public.sessions_horari.id_sessio;


--
-- Name: ausencies id_ausencia; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies ALTER COLUMN id_ausencia SET DEFAULT nextval('public.ausencies_id_ausencia_seq'::regclass);


--
-- Name: docent id_docent; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent ALTER COLUMN id_docent SET DEFAULT nextval('public.docent_id_docent_seq'::regclass);


--
-- Name: docent_sessio id_asignacion; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio ALTER COLUMN id_asignacion SET DEFAULT nextval('public.docent_sessio_id_asignacion_seq'::regclass);


--
-- Name: guardies id_guardia; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies ALTER COLUMN id_guardia SET DEFAULT nextval('public.guardies_id_guardia_seq'::regclass);


--
-- Name: sessions_horari id_sessio; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari ALTER COLUMN id_sessio SET DEFAULT nextval('public.sessions_horari_id_sessio_seq'::regclass);


--
-- Data for Name: ausencies; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.ausencies (id_ausencia, id_docent, fecha_ausencia, is_full_day) FROM stdin;
\.


--
-- Data for Name: ausencies_sessio; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.ausencies_sessio (id_ausencia, id_sessio) FROM stdin;
\.


--
-- Data for Name: docent; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.docent (id_docent, document, nom, cognom1, cognom2, tipus_doc, sexe, data_ingres, hores_lloc, hores_dedicades, data_naix, ensenyament, organisme, username, password_hash, rol) FROM stdin;
\.


--
-- Data for Name: docent_sessio; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.docent_sessio (id_asignacion, id_docent, id_sessio, ocupacion) FROM stdin;
\.


--
-- Data for Name: guardies; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.guardies (id_guardia, docent_assignat, docent_absent, id_sessio, fecha_guardia) FROM stdin;
\.


--
-- Data for Name: sessions_horari; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.sessions_horari (id_sessio, plantilla, dia_setmana, sessio_ordre, hora_desde, hora_fins) FROM stdin;
\.


--
-- Name: ausencies_id_ausencia_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.ausencies_id_ausencia_seq', 1, false);


--
-- Name: docent_id_docent_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.docent_id_docent_seq', 1, false);


--
-- Name: docent_sessio_id_asignacion_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.docent_sessio_id_asignacion_seq', 1, false);


--
-- Name: guardies_id_guardia_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.guardies_id_guardia_seq', 1, false);


--
-- Name: sessions_horari_id_sessio_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.sessions_horari_id_sessio_seq', 1, false);


--
-- Name: ausencies ausencies_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies
    ADD CONSTRAINT ausencies_pkey PRIMARY KEY (id_ausencia);


--
-- Name: ausencies_sessio ausencies_sessio_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT ausencies_sessio_pkey PRIMARY KEY (id_ausencia, id_sessio);


--
-- Name: docent docent_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent
    ADD CONSTRAINT docent_pkey PRIMARY KEY (id_docent);


--
-- Name: docent_sessio docent_sessio_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio
    ADD CONSTRAINT docent_sessio_pkey PRIMARY KEY (id_asignacion);


--
-- Name: docent docent_username_key; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent
    ADD CONSTRAINT docent_username_key UNIQUE (username);


--
-- Name: guardies guardies_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT guardies_pkey PRIMARY KEY (id_guardia);


--
-- Name: sessions_horari sessions_horari_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari
    ADD CONSTRAINT sessions_horari_pkey PRIMARY KEY (id_sessio);


--
-- Name: guardies fk_absent; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT fk_absent FOREIGN KEY (docent_absent) REFERENCES public.docent(id_docent);


--
-- Name: guardies fk_assignat; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT fk_assignat FOREIGN KEY (docent_assignat) REFERENCES public.docent(id_docent);


--
-- Name: ausencies_sessio fk_ausencia; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT fk_ausencia FOREIGN KEY (id_ausencia) REFERENCES public.ausencies(id_ausencia);


--
-- Name: docent_sessio fk_docent; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio
    ADD CONSTRAINT fk_docent FOREIGN KEY (id_docent) REFERENCES public.docent(id_docent);


--
-- Name: ausencies fk_docent_ausencia; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies
    ADD CONSTRAINT fk_docent_ausencia FOREIGN KEY (id_docent) REFERENCES public.docent(id_docent);


--
-- Name: guardies fk_guardia_sessio; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT fk_guardia_sessio FOREIGN KEY (id_sessio) REFERENCES public.sessions_horari(id_sessio);


--
-- Name: docent_sessio fk_sessio; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio
    ADD CONSTRAINT fk_sessio FOREIGN KEY (id_sessio) REFERENCES public.sessions_horari(id_sessio);


--
-- Name: ausencies_sessio fk_sessio_ausencia; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT fk_sessio_ausencia FOREIGN KEY (id_sessio) REFERENCES public.sessions_horari(id_sessio);


--
-- PostgreSQL database dump complete
--

