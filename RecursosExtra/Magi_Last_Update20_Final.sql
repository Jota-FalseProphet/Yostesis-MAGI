--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9 (Ubuntu 16.9-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.9 (Ubuntu 16.9-0ubuntu0.24.04.1)

-- Started on 2025-06-04 19:35:08 CEST

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

--
-- TOC entry 6 (class 2615 OID 17443)
-- Name: guardias; Type: SCHEMA; Schema: -; Owner: magiuser
--

CREATE SCHEMA guardias;


ALTER SCHEMA guardias OWNER TO magiuser;

--
-- TOC entry 7 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: magiuser
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO magiuser;

--
-- TOC entry 3610 (class 0 OID 0)
-- Dependencies: 7
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: magiuser
--

COMMENT ON SCHEMA public IS '';


--
-- TOC entry 2 (class 3079 OID 17444)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 3612 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 290 (class 1255 OID 17481)
-- Name: autogenera_ausencias(interval, text); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.autogenera_ausencias(p_grace interval DEFAULT '00:05:00'::interval, p_plantilla text DEFAULT '366781135'::text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_ahora   time without time zone;
  rec_abs   RECORD;
  v_count   integer := 0;
BEGIN
  v_ahora := timezone('Europe/Madrid', now())::time;

  FOR rec_abs IN
    SELECT id_ausencia
      FROM public.ausencies
     WHERE fecha_ausencia = CURRENT_DATE
       AND NOT EXISTS (
         SELECT 1
           FROM public.ausencies_sessio s
          WHERE s.id_ausencia = public.ausencies.id_ausencia
            AND s.id_sessio   = 91
       )
  LOOP
    IF EXISTS (
      SELECT 1
        FROM public.sessions_horari sh
       WHERE sh.id_sessio    = 91
         AND v_ahora        >= sh.hora_desde + p_grace
    ) THEN
      INSERT INTO public.ausencies_sessio(id_ausencia, id_sessio)
      VALUES (rec_abs.id_ausencia, 91)
      ON CONFLICT DO NOTHING;
      v_count := v_count + 1;
    END IF;
  END LOOP;

  RETURN v_count;
END;
$$;


ALTER FUNCTION public.autogenera_ausencias(p_grace interval, p_plantilla text) OWNER TO magiuser;

--
-- TOC entry 291 (class 1255 OID 17482)
-- Name: autogenera_registro_ausencias(interval, text); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.autogenera_registro_ausencias(p_grace interval DEFAULT '00:05:00'::interval, p_plantilla text DEFAULT '366781135'::text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_now       time   := timezone('Europe/Madrid', now())::time;
  v_dow       integer:= EXTRACT(dow FROM timezone('Europe/Madrid', now()))::int;
  v_dia       text;
  rec         RECORD;
  v_ausencia  integer;
  v_count     integer:= 0;
BEGIN
  v_dia := CASE v_dow
    WHEN 0 THEN 'Domingo'
    WHEN 1 THEN 'Lunes'
    WHEN 2 THEN 'Martes'
    WHEN 3 THEN 'Miércoles'
    WHEN 4 THEN 'Jueves'
    WHEN 5 THEN 'Viernes'
    WHEN 6 THEN 'Sábado'
  END;

  FOR rec IN
    SELECT ds.id_docent,
           d.document    AS docente_dni,
           ds.id_sessio,
           sh.hora_desde
      FROM public.docent_sessio ds
      JOIN public.sessions_horari sh
        ON sh.id_sessio = ds.id_sessio
      JOIN public.docent d
        ON d.id_docent = ds.id_docent
     WHERE sh.plantilla   = p_plantilla
       AND sh.dia_setmana = v_dia
       AND (sh.hora_desde + p_grace) <= v_now
       AND NOT EXISTS (
         SELECT 1
           FROM public.guardies g
          WHERE g.id_sessio     = ds.id_sessio
            AND g.fecha_guardia = CURRENT_DATE
       )
       AND NOT EXISTS (
         SELECT 1
           FROM public.fichajes f
          WHERE f.usuario_id   = d.document
            AND f.fecha        = CURRENT_DATE
            AND f.hora_inicio <= sh.hora_desde + p_grace
       )
  LOOP
    SELECT a.id_ausencia
      INTO v_ausencia
      FROM public.ausencies a
     WHERE a.id_docent      = rec.id_docent
       AND a.fecha_ausencia = CURRENT_DATE
     LIMIT 1;

    IF NOT FOUND THEN
      INSERT INTO public.ausencies(id_docent, fecha_ausencia, is_full_day)
      VALUES (rec.id_docent, CURRENT_DATE, FALSE)
      RETURNING id_ausencia INTO v_ausencia;
    END IF;

    INSERT INTO public.ausencies_sessio(id_ausencia, id_sessio)
    VALUES (v_ausencia, rec.id_sessio)
    ON CONFLICT DO NOTHING;

    v_count := v_count + 1;
  END LOOP;

  RETURN v_count;
END;
$$;


ALTER FUNCTION public.autogenera_registro_ausencias(p_grace interval, p_plantilla text) OWNER TO magiuser;

--
-- TOC entry 292 (class 1255 OID 17483)
-- Name: check_guardia_tiempo(); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.check_guardia_tiempo() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_fin       time;
  v_now_time  time := timezone('Europe/Madrid', now())::time;
  v_now_date  date := timezone('Europe/Madrid', now())::date;
BEGIN
  SELECT sh.hora_fins
    INTO v_fin
    FROM public.sessions_horari AS sh
   WHERE sh.id_sessio = NEW.id_sessio;

  IF NEW.fecha_guardia = v_now_date
     AND v_now_time > v_fin
  THEN
    RAISE EXCEPTION 'No se puede asignar guardia: la sesión % finalizó a las %', 
      NEW.id_sessio, v_fin
    USING ERRCODE = '22023';  
  END IF;

  RETURN NEW;
END;
$$;


ALTER FUNCTION public.check_guardia_tiempo() OWNER TO magiuser;

--
-- TOC entry 293 (class 1255 OID 17484)
-- Name: crea_guardia_desde_ausencia(integer); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.crea_guardia_desde_ausencia(p_ausencia integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  v_s        record;
  v_doc      int;
  v_date     date;
  v_now_date date := timezone('Europe/Madrid', now())::date;
  v_now_time time := timezone('Europe/Madrid', now())::time;
BEGIN
 
  SELECT id_docent, fecha_ausencia
    INTO v_doc, v_date
    FROM public.ausencies
   WHERE id_ausencia = p_ausencia;

  FOR v_s IN
    SELECT aus.id_sessio
      FROM public.ausencies_sessio AS aus
      JOIN public.sessions_horari AS sh
        ON aus.id_sessio = sh.id_sessio
     WHERE aus.id_ausencia = p_ausencia
       AND (
       
         v_date > v_now_date
         OR
       
         (v_date = v_now_date AND sh.hora_fins > v_now_time)
       )
  LOOP
    INSERT INTO public.guardies
      (docent_assignat, docent_absent, id_sessio, fecha_guardia)
    VALUES
      (NULL, v_doc, v_s.id_sessio, v_date)
    ON CONFLICT DO NOTHING;
  END LOOP;
END;
$$;


ALTER FUNCTION public.crea_guardia_desde_ausencia(p_ausencia integer) OWNER TO magiuser;

--
-- TOC entry 294 (class 1255 OID 17485)
-- Name: revoca_ausencia(); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.revoca_ausencia() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  UPDATE public.ausencies
     SET anulada = true
   WHERE id_docent = NEW.id_docent
     AND fecha_ausencia = NEW.fecha
     AND anulada = false;
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.revoca_ausencia() OWNER TO magiuser;

--
-- TOC entry 295 (class 1255 OID 17486)
-- Name: sync_docent_usuario(); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.sync_docent_usuario() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  INSERT INTO usuarios(dni, nombre, password, rol)
    VALUES (
      NEW.document,
      COALESCE(NEW.nom,'') || ' ' || COALESCE(NEW.cognom1,''),  
      'changeme',                                               
      'DOCENT'                                               
    )
  ON CONFLICT (dni) DO UPDATE
    SET nombre = EXCLUDED.nombre;  
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.sync_docent_usuario() OWNER TO magiuser;

--
-- TOC entry 278 (class 1255 OID 17487)
-- Name: usuarios_to_docent(); Type: FUNCTION; Schema: public; Owner: magiuser
--

CREATE FUNCTION public.usuarios_to_docent() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NEW.rol = 'PROFESOR' THEN
    INSERT INTO docent (document, nom, cognom1, username, password_hash, rol)
      VALUES (
        NEW.dni,
        split_part(NEW.nombre,' ',1),
        split_part(NEW.nombre,' ',2),
        NEW.dni,
        NEW.password,
        NEW.rol
      )
    ON CONFLICT (document) DO NOTHING;
  END IF;
  RETURN NEW;
END;
$$;


ALTER FUNCTION public.usuarios_to_docent() OWNER TO magiuser;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 17488)
-- Name: staging_aules; Type: TABLE; Schema: guardias; Owner: magiuser
--

CREATE TABLE guardias.staging_aules (
    codi character varying(10) NOT NULL,
    aula character varying(10) NOT NULL
);


ALTER TABLE guardias.staging_aules OWNER TO magiuser;

--
-- TOC entry 218 (class 1259 OID 17491)
-- Name: staging_docent; Type: TABLE; Schema: guardias; Owner: magiuser
--

CREATE TABLE guardias.staging_docent (
    nom character varying(50) NOT NULL,
    cognom1 character varying(50) NOT NULL,
    cognom2 character varying(50) NOT NULL,
    tipo_doc character varying(1) NOT NULL,
    document character varying(10) NOT NULL,
    sexe character varying(1),
    data_ingres character varying(10),
    hores_lloc character varying(5) NOT NULL,
    hores_dedicades character varying(5) NOT NULL,
    data_naix character varying(10),
    ensenyament boolean NOT NULL,
    organisme boolean NOT NULL
);


ALTER TABLE guardias.staging_docent OWNER TO magiuser;

--
-- TOC entry 219 (class 1259 OID 17494)
-- Name: staging_sessions; Type: TABLE; Schema: guardias; Owner: magiuser
--

CREATE TABLE guardias.staging_sessions (
    id integer NOT NULL,
    plantilla character varying(9) NOT NULL,
    dia_setmana character(1) NOT NULL,
    sessio_orde character varying(2) NOT NULL,
    hora_desde character varying(5) NOT NULL,
    hora_fins character varying(5) NOT NULL
);


ALTER TABLE guardias.staging_sessions OWNER TO magiuser;

--
-- TOC entry 220 (class 1259 OID 17497)
-- Name: aula; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.aula (
    id_aula integer NOT NULL,
    nombre character varying(50) NOT NULL
);


ALTER TABLE public.aula OWNER TO magiuser;

--
-- TOC entry 221 (class 1259 OID 17500)
-- Name: aula_id_aula_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.aula_id_aula_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.aula_id_aula_seq OWNER TO magiuser;

--
-- TOC entry 3613 (class 0 OID 0)
-- Dependencies: 221
-- Name: aula_id_aula_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.aula_id_aula_seq OWNED BY public.aula.id_aula;


--
-- TOC entry 222 (class 1259 OID 17501)
-- Name: ausencies; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.ausencies (
    id_ausencia integer NOT NULL,
    id_docent integer NOT NULL,
    fecha_ausencia date NOT NULL,
    is_full_day boolean DEFAULT false,
    anulada boolean DEFAULT false,
    motivo character varying(30) DEFAULT 'GENERAL'::character varying NOT NULL
);


ALTER TABLE public.ausencies OWNER TO magiuser;

--
-- TOC entry 223 (class 1259 OID 17506)
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
-- TOC entry 3614 (class 0 OID 0)
-- Dependencies: 223
-- Name: ausencies_id_ausencia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.ausencies_id_ausencia_seq OWNED BY public.ausencies.id_ausencia;


--
-- TOC entry 224 (class 1259 OID 17507)
-- Name: ausencies_sessio; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.ausencies_sessio (
    id_ausencia integer NOT NULL,
    id_sessio integer NOT NULL
);


ALTER TABLE public.ausencies_sessio OWNER TO magiuser;

--
-- TOC entry 225 (class 1259 OID 17510)
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
-- TOC entry 226 (class 1259 OID 17515)
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
-- TOC entry 3615 (class 0 OID 0)
-- Dependencies: 226
-- Name: docent_id_docent_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.docent_id_docent_seq OWNED BY public.docent.id_docent;


--
-- TOC entry 227 (class 1259 OID 17516)
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
-- TOC entry 228 (class 1259 OID 17519)
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
-- TOC entry 3616 (class 0 OID 0)
-- Dependencies: 228
-- Name: docent_sessio_id_asignacion_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.docent_sessio_id_asignacion_seq OWNED BY public.docent_sessio.id_asignacion;


--
-- TOC entry 229 (class 1259 OID 17520)
-- Name: fichaje; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.fichaje (
    id bigint NOT NULL,
    dni character varying(10) NOT NULL,
    fecha_hora timestamp without time zone NOT NULL,
    tipo character varying(3) NOT NULL,
    CONSTRAINT fichaje_tipo_check CHECK (((tipo)::text = ANY (ARRAY[('IN'::character varying)::text, ('OUT'::character varying)::text])))
);


ALTER TABLE public.fichaje OWNER TO magiuser;

--
-- TOC entry 230 (class 1259 OID 17524)
-- Name: fichaje_id_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.fichaje_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.fichaje_id_seq OWNER TO magiuser;

--
-- TOC entry 3617 (class 0 OID 0)
-- Dependencies: 230
-- Name: fichaje_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.fichaje_id_seq OWNED BY public.fichaje.id;


--
-- TOC entry 231 (class 1259 OID 17525)
-- Name: fichajes; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.fichajes (
    id bigint NOT NULL,
    fecha date NOT NULL,
    hora_fin time(6) without time zone,
    hora_inicio time(6) without time zone,
    usuario_id character varying(12) NOT NULL,
    total interval DEFAULT '00:00:00'::interval NOT NULL
);


ALTER TABLE public.fichajes OWNER TO magiuser;

--
-- TOC entry 232 (class 1259 OID 17529)
-- Name: fichajes_id_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

ALTER TABLE public.fichajes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.fichajes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 233 (class 1259 OID 17530)
-- Name: grupo; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.grupo (
    id_grupo integer NOT NULL,
    nombre character varying(50) NOT NULL,
    etapa character varying(20),
    curso character varying(10)
);


ALTER TABLE public.grupo OWNER TO magiuser;

--
-- TOC entry 234 (class 1259 OID 17533)
-- Name: grupo_id_grupo_seq; Type: SEQUENCE; Schema: public; Owner: magiuser
--

CREATE SEQUENCE public.grupo_id_grupo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.grupo_id_grupo_seq OWNER TO magiuser;

--
-- TOC entry 3618 (class 0 OID 0)
-- Dependencies: 234
-- Name: grupo_id_grupo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.grupo_id_grupo_seq OWNED BY public.grupo.id_grupo;


--
-- TOC entry 235 (class 1259 OID 17534)
-- Name: guardies; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.guardies (
    id_guardia integer NOT NULL,
    docent_assignat integer,
    docent_absent integer NOT NULL,
    id_sessio integer NOT NULL,
    fecha_guardia date NOT NULL
);


ALTER TABLE public.guardies OWNER TO magiuser;

--
-- TOC entry 236 (class 1259 OID 17537)
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
-- TOC entry 3619 (class 0 OID 0)
-- Dependencies: 236
-- Name: guardies_id_guardia_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.guardies_id_guardia_seq OWNED BY public.guardies.id_guardia;


--
-- TOC entry 237 (class 1259 OID 17538)
-- Name: sessions_horari; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.sessions_horari (
    id_sessio integer NOT NULL,
    plantilla character varying(50),
    dia_setmana character varying(20),
    sessio_ordre integer,
    hora_desde time without time zone,
    hora_fins time without time zone,
    id_aula integer,
    id_grupo integer
);


ALTER TABLE public.sessions_horari OWNER TO magiuser;

--
-- TOC entry 238 (class 1259 OID 17541)
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
-- TOC entry 3620 (class 0 OID 0)
-- Dependencies: 238
-- Name: sessions_horari_id_sessio_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: magiuser
--

ALTER SEQUENCE public.sessions_horari_id_sessio_seq OWNED BY public.sessions_horari.id_sessio;


--
-- TOC entry 239 (class 1259 OID 17542)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: magiuser
--

CREATE TABLE public.usuarios (
    dni character varying(12) NOT NULL,
    nombre character varying(60),
    password character varying(72),
    rol character varying(20)
);


ALTER TABLE public.usuarios OWNER TO magiuser;

--
-- TOC entry 241 (class 1259 OID 17691)
-- Name: vw_faltas; Type: VIEW; Schema: public; Owner: magiuser
--

CREATE VIEW public.vw_faltas AS
 SELECT a.id_ausencia,
    s.id_sessio,
    a.fecha_ausencia AS fecha,
    d.id_docent AS id_docente,
    concat(d.nom, ' ', d.cognom1) AS docente,
    g.id_guardia,
    g.docent_assignat,
    concat(dg.nom, ' ', dg.cognom1) AS docente_guardia,
    sh.id_grupo,
    gr.nombre AS grupo,
    gr.etapa,
    sh.hora_desde,
    sh.hora_fins
   FROM ((((((public.ausencies a
     JOIN public.ausencies_sessio s ON ((s.id_ausencia = a.id_ausencia)))
     JOIN public.sessions_horari sh ON ((sh.id_sessio = s.id_sessio)))
     JOIN public.grupo gr ON ((gr.id_grupo = sh.id_grupo)))
     JOIN public.docent d ON ((d.id_docent = a.id_docent)))
     LEFT JOIN public.guardies g ON (((g.id_sessio = s.id_sessio) AND (g.fecha_guardia = a.fecha_ausencia) AND (g.docent_absent = a.id_docent))))
     LEFT JOIN public.docent dg ON ((dg.id_docent = g.docent_assignat)));


ALTER VIEW public.vw_faltas OWNER TO magiuser;

--
-- TOC entry 240 (class 1259 OID 17545)
-- Name: vw_horario_docente_sesion; Type: VIEW; Schema: public; Owner: magiuser
--

CREATE VIEW public.vw_horario_docente_sesion AS
 SELECT sh.id_sessio AS id_sesion,
    sh.plantilla,
    sh.dia_setmana AS dia_semana,
    sh.sessio_ordre AS orden_sesion,
    sh.hora_desde,
    sh.hora_fins AS hora_hasta,
    ds.id_docent AS id_docente,
    concat(d.nom, ' ', d.cognom1, ' ', d.cognom2) AS nombre_docente,
    ds.ocupacion
   FROM ((public.sessions_horari sh
     JOIN public.docent_sessio ds ON ((ds.id_sessio = sh.id_sessio)))
     JOIN public.docent d ON ((d.id_docent = ds.id_docent)));


ALTER VIEW public.vw_horario_docente_sesion OWNER TO magiuser;

--
-- TOC entry 3362 (class 2604 OID 17550)
-- Name: aula id_aula; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.aula ALTER COLUMN id_aula SET DEFAULT nextval('public.aula_id_aula_seq'::regclass);


--
-- TOC entry 3363 (class 2604 OID 17551)
-- Name: ausencies id_ausencia; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies ALTER COLUMN id_ausencia SET DEFAULT nextval('public.ausencies_id_ausencia_seq'::regclass);


--
-- TOC entry 3367 (class 2604 OID 17552)
-- Name: docent id_docent; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent ALTER COLUMN id_docent SET DEFAULT nextval('public.docent_id_docent_seq'::regclass);


--
-- TOC entry 3368 (class 2604 OID 17553)
-- Name: docent_sessio id_asignacion; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio ALTER COLUMN id_asignacion SET DEFAULT nextval('public.docent_sessio_id_asignacion_seq'::regclass);


--
-- TOC entry 3369 (class 2604 OID 17554)
-- Name: fichaje id; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.fichaje ALTER COLUMN id SET DEFAULT nextval('public.fichaje_id_seq'::regclass);


--
-- TOC entry 3371 (class 2604 OID 17555)
-- Name: grupo id_grupo; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.grupo ALTER COLUMN id_grupo SET DEFAULT nextval('public.grupo_id_grupo_seq'::regclass);


--
-- TOC entry 3372 (class 2604 OID 17556)
-- Name: guardies id_guardia; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies ALTER COLUMN id_guardia SET DEFAULT nextval('public.guardies_id_guardia_seq'::regclass);


--
-- TOC entry 3373 (class 2604 OID 17557)
-- Name: sessions_horari id_sessio; Type: DEFAULT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari ALTER COLUMN id_sessio SET DEFAULT nextval('public.sessions_horari_id_sessio_seq'::regclass);


--
-- TOC entry 3582 (class 0 OID 17488)
-- Dependencies: 217
-- Data for Name: staging_aules; Type: TABLE DATA; Schema: guardias; Owner: magiuser
--

COPY guardias.staging_aules (codi, aula) FROM stdin;
A00	A00
A01	A01
A02	A02
A03	A03
A04	A04
A10	A10
A11	A11
A12	A12
A13	A13
A14	A14
A15	A15
A16	A16
A17	A17
A18	A18
A19	A19
B01	B01
B02	B02
B03	B03
B04	B04
B05	B05
B11	B11
B12	B12
B13	B13
B14	B14
B15	B15
B16	B16
B17	B17
C01	C01
C11	C11
C12	C12
C13	C13
C21	C21
CODI	AULA
D01	D01
D02	D02
D03	D03
D04	D04
D05	D05
D06	D06
D11	D11
D12	D12
D13	D13
D14	D14
D15	D15
PATI	PATI
SALAACT	SALAACT
A00	A00
A01	A01
A02	A02
A03	A03
A04	A04
A10	A10
A11	A11
A12	A12
A13	A13
A14	A14
A15	A15
A16	A16
A17	A17
A18	A18
A19	A19
B01	B01
B02	B02
B03	B03
B04	B04
B05	B05
B11	B11
B12	B12
B13	B13
B14	B14
B15	B15
B16	B16
B17	B17
C01	C01
C11	C11
C12	C12
C13	C13
C21	C21
CODI	AULA
D01	D01
D02	D02
D03	D03
D04	D04
D05	D05
D06	D06
D11	D11
D12	D12
D13	D13
D14	D14
D15	D15
PATI	PATI
SALAACT	SALAACT
\.


--
-- TOC entry 3583 (class 0 OID 17491)
-- Dependencies: 218
-- Data for Name: staging_docent; Type: TABLE DATA; Schema: guardias; Owner: magiuser
--

COPY guardias.staging_docent (nom, cognom1, cognom2, tipo_doc, document, sexe, data_ingres, hores_lloc, hores_dedicades, data_naix, ensenyament, organisme) FROM stdin;
ANGEL	A	C	N	010960328X	H	01/09/2004	20:00	20:00	14/11/1960	t	t
FRANCISCO JAVIER	F	C	N	010969028D	H	01/09/2010	20:00	20:00	15/12/1956	t	t
ANGEL	R	M	N	011078949R	H	01/09/2015	20:00	20:00	12/11/1964	t	t
MARCOS ANTONIO	O	Q	N	011921608R	H	01/09/2015	20:00	20:00	23/11/1967	t	t
JUAN ANTONIO	M	B	N	011939238D	H	01/09/2006	20:00	20:00	28/07/1960	t	t
JOSEP LLUIS	B	C	N	011949328W	H	01/09/2007	20:00	20:00	05/07/1963	t	t
GABRIEL	C	A	N	011973418Z	H	01/09/2004	20:00	20:00	09/08/1964	t	t
ANGELES	B	C	N	012855339X	M	01/09/2015	20:00	20:00	24/03/1958	t	t
JOSE VICENTE	G	B	N	012905318D	H	01/09/2004	20:00	20:00	18/03/1961	t	t
PASCUAL MIGUEL	P	B	N	012950628N	H	01/09/2015	20:00	20:00	04/10/1966	t	t
PERE	F	G	N	012951758W	H	01/09/2015	20:00	20:00	03/08/1973	t	t
YOLANDA	G	M	N	012986535F	M	01/09/2015	20:00	20:00	24/01/1962	t	t
JOSEP ENRIC	E	A	N	013906018J	H	17/06/2007	20:00	20:00	14/07/1956	t	t
OLGA	S	D	N	013928518A	M	23/03/2015	20:00	20:00	06/11/1965	t	t
ELOY	F	R	N	013931868S	H	01/09/2009	20:00	20:00	26/06/1972	t	t
VICENTA	N	B	N	013962048R	M	01/09/2007	20:00	20:00	22/10/1955	t	t
FRANCESC	D	M	N	014919208N	H	01/09/2004	20:00	20:00	11/10/1961	t	t
CARME	A	H	N	014985308L	M	01/09/2004	20:00	20:00	03/10/1961	t	t
NURIA	C	S	N	015965468J	M	01/09/2004	20:00	20:00	24/01/1964	t	t
JAVIER	M	B	N	016910908H	H	01/09/2015	20:00	20:00	03/07/1973	t	t
MARIA NIEVES	M	F	N	016946918A	M	14/09/2015	20:00	20:00	04/08/1975	t	t
ESTHER	S	O	N	016976848Q	M	01/09/2015	20:00	20:00	01/06/1973	t	t
MARIA JESUS	C	G	N	017050149N	M	29/06/2011	20:00	20:00	09/09/1976	t	t
QUEREMON	R	G	N	017911348P	H	01/09/2004	20:00	20:00	26/01/1961	t	t
MARIA DE BEGOÑA	E	C	N	017959408Z	M	01/09/2005	20:00	20:00	16/06/1965	t	t
MONICA	G	A	N	019080189D	M	01/09/2015	20:00	20:00	22/11/1977	t	t
ASENSIO	M	S	N	019825419Z	H	01/09/2015	20:00	20:00	05/06/1962	t	t
JESUS	A	M	N	019900748V	H	01/09/2007	20:00	20:00	10/01/1967	t	t
SILVIA MERCEDES	Q	I	N	020446640M	M	01/09/2015	20:00	20:00	21/03/1981	t	t
RUT	A	H	N	022615771H	M	01/09/2015	20:00	20:00	18/08/1979	t	t
JOSE	V	R	N	023440660P	H	01/09/2015	20:00	20:00	14/08/1980	t	t
HECTOR ANTONIO	G	A	N	024163252T	H	01/09/2004	20:00	20:00	09/03/1962	t	t
ISABEL	M	R	N	024389784E	M	25/09/2015	20:00	20:00	20/12/1979	t	t
IGNACIO	S	A	N	024453065H	H	01/09/2008	20:00	20:00	22/04/1967	t	t
MARIA DEL CARMEN	L	G	N	025347434A	M	09/09/2015	20:00	20:00	30/04/1971	t	t
SACRAMENTO	T	R	N	025470191A	M	01/09/2004	20:00	20:00	04/12/1957	t	t
ALEJANDRO	T	Z	N	026577702D	H	01/09/2015	20:00	20:00	29/12/1978	t	t
FRANK F.	W	L	N	026715900R	H	01/09/2015	20:00	20:00	30/05/1964	t	t
MARIA	J	P	N	027246470Z	M	01/09/2015	20:00	20:00	10/02/1979	t	t
SOFIA	P	P	N	028203430W	M	25/09/2015	20:00	20:00	15/03/1979	t	t
BEGOÑA	F	S	N	028301344S	M	01/09/2005	20:00	20:00	26/05/1963	t	t
MIGUEL	G	H	N	028376971Q	H	01/09/2004	20:00	20:00	15/08/1956	t	t
JORGE	R	M	N	029218450V	H	01/09/2015	20:00	20:00	04/01/1982	t	t
EVA	V	B	N	031478003M	M	01/09/2009	20:00	20:00	16/03/1972	t	t
AURORA	E	M	N	033580118G	M	01/09/2013	20:00	20:00	11/01/1966	t	t
JAVIER	A	E	N	039660939E	H	01/09/2007	20:00	20:00	03/02/1971	t	t
ANA	U	P	N	041346258R	M	14/09/2015	20:00	20:00	23/12/1982	t	t
NURIA	P	G	N	045760203C	M	01/07/2010	20:00	20:00	17/09/1973	t	t
FERRAN JOSEP	R	P	N	045989240B	H	01/09/2004	20:00	20:00	18/01/1969	t	t
MARIA FRANCISCA	M	A	N	046417441L	M	01/09/2015	20:00	20:00	21/04/1967	t	t
ALEIX	C	M	N	046730283P	H	01/09/2009	20:00	20:00	31/12/1972	t	t
DANIEL	M	M	N	047397438M	H	01/09/2014	20:00	20:00	14/04/1977	t	t
JOSE RAMON	M	G	N	047928150S	H	01/09/2004	20:00	20:00	19/09/1961	t	t
AGUSTIN	G	B	N	054309783V	H	01/09/2015	20:00	20:00	30/10/1984	t	t
SUSANA	N	P	N	055742962J	M	01/09/2015	20:00	20:00	01/04/1968	t	t
JOSE ANTONIO	P	O	N	059603111F	H	01/10/1989	20:00	20:00	20/08/1955	t	t
ANTONIO	B	P	N	070094824B	H	01/09/2013	20:00	20:00	22/05/1965	t	t
SANTIAGO	M	F	N	070374673Z	H	01/09/2004	20:00	20:00	12/07/1953	t	t
LIDIA	C	R	N	070391913Z	M	01/09/2015	20:00	20:00	17/11/1979	t	t
MARIA ISABEL	C	O	N	072516423B	M	01/09/2004	20:00	20:00	02/10/1963	t	t
ANTONIO	C	S	N	075035864L	H	01/09/2010	20:00	20:00	03/02/1965	t	t
ROSA MARIA	P	S	N	075345863D	M	01/09/2012	20:00	20:00	04/04/1969	t	t
DIEGO	A	P	N	076304893C	H	01/09/2014	20:00	20:00	19/04/1966	t	t
JOSE MIGUEL	F	P	N	076335803B	H	01/09/2015	20:00	20:00	28/10/1968	t	t
JOSE	O	L	N	078229283W	H	01/09/2004	20:00	20:00	23/08/1958	t	t
JOSE MANUEL	L	R	N	078308873T	H	01/09/2009	20:00	20:00	26/04/1973	t	t
M. TERESA	R	G	N	078395783P	M	01/09/2015	20:00	20:00	25/12/1958	t	t
JUAN	F	B	N	078399623C	H	01/09/2004	20:00	20:00	03/01/1956	t	t
MANUEL	S	O	N	079320803C	H	01/09/2004	20:00	20:00	01/05/1962	t	t
MERCEDES DEL CARMEN	J	E	N	079351863T	M	01/09/2005	20:00	20:00	16/09/1963	t	t
EMILIE	B	 	E	X09610120P	M	01/09/2011	20:00	20:00	26/07/1959	t	t
ANGEL	A	C	N	010960328X	H	01/09/2004	20:00	20:00	14/11/1960	t	t
FRANCISCO JAVIER	F	C	N	010969028D	H	01/09/2010	20:00	20:00	15/12/1956	t	t
ANGEL	R	M	N	011078949R	H	01/09/2015	20:00	20:00	12/11/1964	t	t
MARCOS ANTONIO	O	Q	N	011921608R	H	01/09/2015	20:00	20:00	23/11/1967	t	t
JUAN ANTONIO	M	B	N	011939238D	H	01/09/2006	20:00	20:00	28/07/1960	t	t
JOSEP LLUIS	B	C	N	011949328W	H	01/09/2007	20:00	20:00	05/07/1963	t	t
GABRIEL	C	A	N	011973418Z	H	01/09/2004	20:00	20:00	09/08/1964	t	t
ANGELES	B	C	N	012855339X	M	01/09/2015	20:00	20:00	24/03/1958	t	t
JOSE VICENTE	G	B	N	012905318D	H	01/09/2004	20:00	20:00	18/03/1961	t	t
PASCUAL MIGUEL	P	B	N	012950628N	H	01/09/2015	20:00	20:00	04/10/1966	t	t
PERE	F	G	N	012951758W	H	01/09/2015	20:00	20:00	03/08/1973	t	t
YOLANDA	G	M	N	012986535F	M	01/09/2015	20:00	20:00	24/01/1962	t	t
JOSEP ENRIC	E	A	N	013906018J	H	17/06/2007	20:00	20:00	14/07/1956	t	t
OLGA	S	D	N	013928518A	M	23/03/2015	20:00	20:00	06/11/1965	t	t
ELOY	F	R	N	013931868S	H	01/09/2009	20:00	20:00	26/06/1972	t	t
VICENTA	N	B	N	013962048R	M	01/09/2007	20:00	20:00	22/10/1955	t	t
FRANCESC	D	M	N	014919208N	H	01/09/2004	20:00	20:00	11/10/1961	t	t
CARME	A	H	N	014985308L	M	01/09/2004	20:00	20:00	03/10/1961	t	t
NURIA	C	S	N	015965468J	M	01/09/2004	20:00	20:00	24/01/1964	t	t
JAVIER	M	B	N	016910908H	H	01/09/2015	20:00	20:00	03/07/1973	t	t
MARIA NIEVES	M	F	N	016946918A	M	14/09/2015	20:00	20:00	04/08/1975	t	t
ESTHER	S	O	N	016976848Q	M	01/09/2015	20:00	20:00	01/06/1973	t	t
MARIA JESUS	C	G	N	017050149N	M	29/06/2011	20:00	20:00	09/09/1976	t	t
QUEREMON	R	G	N	017911348P	H	01/09/2004	20:00	20:00	26/01/1961	t	t
MARIA DE BEGOÑA	E	C	N	017959408Z	M	01/09/2005	20:00	20:00	16/06/1965	t	t
MONICA	G	A	N	019080189D	M	01/09/2015	20:00	20:00	22/11/1977	t	t
ASENSIO	M	S	N	019825419Z	H	01/09/2015	20:00	20:00	05/06/1962	t	t
JESUS	A	M	N	019900748V	H	01/09/2007	20:00	20:00	10/01/1967	t	t
SILVIA MERCEDES	Q	I	N	020446640M	M	01/09/2015	20:00	20:00	21/03/1981	t	t
RUT	A	H	N	022615771H	M	01/09/2015	20:00	20:00	18/08/1979	t	t
JOSE	V	R	N	023440660P	H	01/09/2015	20:00	20:00	14/08/1980	t	t
HECTOR ANTONIO	G	A	N	024163252T	H	01/09/2004	20:00	20:00	09/03/1962	t	t
ISABEL	M	R	N	024389784E	M	25/09/2015	20:00	20:00	20/12/1979	t	t
IGNACIO	S	A	N	024453065H	H	01/09/2008	20:00	20:00	22/04/1967	t	t
MARIA DEL CARMEN	L	G	N	025347434A	M	09/09/2015	20:00	20:00	30/04/1971	t	t
SACRAMENTO	T	R	N	025470191A	M	01/09/2004	20:00	20:00	04/12/1957	t	t
ALEJANDRO	T	Z	N	026577702D	H	01/09/2015	20:00	20:00	29/12/1978	t	t
FRANK F.	W	L	N	026715900R	H	01/09/2015	20:00	20:00	30/05/1964	t	t
MARIA	J	P	N	027246470Z	M	01/09/2015	20:00	20:00	10/02/1979	t	t
SOFIA	P	P	N	028203430W	M	25/09/2015	20:00	20:00	15/03/1979	t	t
BEGOÑA	F	S	N	028301344S	M	01/09/2005	20:00	20:00	26/05/1963	t	t
MIGUEL	G	H	N	028376971Q	H	01/09/2004	20:00	20:00	15/08/1956	t	t
JORGE	R	M	N	029218450V	H	01/09/2015	20:00	20:00	04/01/1982	t	t
EVA	V	B	N	031478003M	M	01/09/2009	20:00	20:00	16/03/1972	t	t
AURORA	E	M	N	033580118G	M	01/09/2013	20:00	20:00	11/01/1966	t	t
JAVIER	A	E	N	039660939E	H	01/09/2007	20:00	20:00	03/02/1971	t	t
ANA	U	P	N	041346258R	M	14/09/2015	20:00	20:00	23/12/1982	t	t
NURIA	P	G	N	045760203C	M	01/07/2010	20:00	20:00	17/09/1973	t	t
FERRAN JOSEP	R	P	N	045989240B	H	01/09/2004	20:00	20:00	18/01/1969	t	t
MARIA FRANCISCA	M	A	N	046417441L	M	01/09/2015	20:00	20:00	21/04/1967	t	t
ALEIX	C	M	N	046730283P	H	01/09/2009	20:00	20:00	31/12/1972	t	t
DANIEL	M	M	N	047397438M	H	01/09/2014	20:00	20:00	14/04/1977	t	t
JOSE RAMON	M	G	N	047928150S	H	01/09/2004	20:00	20:00	19/09/1961	t	t
AGUSTIN	G	B	N	054309783V	H	01/09/2015	20:00	20:00	30/10/1984	t	t
SUSANA	N	P	N	055742962J	M	01/09/2015	20:00	20:00	01/04/1968	t	t
JOSE ANTONIO	P	O	N	059603111F	H	01/10/1989	20:00	20:00	20/08/1955	t	t
ANTONIO	B	P	N	070094824B	H	01/09/2013	20:00	20:00	22/05/1965	t	t
SANTIAGO	M	F	N	070374673Z	H	01/09/2004	20:00	20:00	12/07/1953	t	t
LIDIA	C	R	N	070391913Z	M	01/09/2015	20:00	20:00	17/11/1979	t	t
MARIA ISABEL	C	O	N	072516423B	M	01/09/2004	20:00	20:00	02/10/1963	t	t
ANTONIO	C	S	N	075035864L	H	01/09/2010	20:00	20:00	03/02/1965	t	t
ROSA MARIA	P	S	N	075345863D	M	01/09/2012	20:00	20:00	04/04/1969	t	t
DIEGO	A	P	N	076304893C	H	01/09/2014	20:00	20:00	19/04/1966	t	t
JOSE MIGUEL	F	P	N	076335803B	H	01/09/2015	20:00	20:00	28/10/1968	t	t
JOSE	O	L	N	078229283W	H	01/09/2004	20:00	20:00	23/08/1958	t	t
JOSE MANUEL	L	R	N	078308873T	H	01/09/2009	20:00	20:00	26/04/1973	t	t
M. TERESA	R	G	N	078395783P	M	01/09/2015	20:00	20:00	25/12/1958	t	t
JUAN	F	B	N	078399623C	H	01/09/2004	20:00	20:00	03/01/1956	t	t
MANUEL	S	O	N	079320803C	H	01/09/2004	20:00	20:00	01/05/1962	t	t
MERCEDES DEL CARMEN	J	E	N	079351863T	M	01/09/2005	20:00	20:00	16/09/1963	t	t
EMILIE	B	 	E	X09610120P	M	01/09/2011	20:00	20:00	26/07/1959	t	t
\.


--
-- TOC entry 3584 (class 0 OID 17494)
-- Dependencies: 219
-- Data for Name: staging_sessions; Type: TABLE DATA; Schema: guardias; Owner: magiuser
--

COPY guardias.staging_sessions (id, plantilla, dia_setmana, sessio_orde, hora_desde, hora_fins) FROM stdin;
1	366781135	L	1	08:20	09:10
2	366781135	L	2	09:10	10:00
3	366781135	L	3	10:00	10:50
4	366781135	L	14	10:50	11:15
5	366781135	L	4	11:15	12:05
6	366781135	L	5	12:05	12:55
7	366781135	L	15	12:55	13:10
8	366781135	L	6	13:10	14:00
9	366781135	L	7	14:00	14:50
10	366781135	L	10	14:50	16:20
11	366781135	L	8	16:20	17:10
12	366781135	L	9	17:10	18:00
13	366781135	L	10	18:00	18:50
14	366781135	L	11	18:50	19:40
15	366781135	L	15	19:40	20:00
16	366781135	L	12	20:00	20:50
17	366781135	L	13	20:50	21:40
18	366781135	M	1	08:20	09:10
19	366781135	M	2	09:10	10:00
20	366781135	M	3	10:00	10:50
21	366781135	M	14	10:50	11:15
22	366781135	M	4	11:15	12:05
23	366781135	M	5	12:05	12:55
24	366781135	M	15	12:55	13:10
25	366781135	M	6	13:10	14:00
26	366781135	M	7	14:00	14:50
27	366781135	M	10	14:50	16:20
28	366781135	M	8	16:20	17:10
29	366781135	M	9	17:10	18:00
30	366781135	M	10	18:00	18:50
31	366781135	M	11	18:50	19:40
32	366781135	M	15	19:40	20:00
33	366781135	M	12	20:00	20:50
34	366781135	M	13	20:50	21:40
35	366781135	X	1	08:20	09:10
36	366781135	X	2	09:10	10:00
37	366781135	X	3	10:00	10:50
38	366781135	X	14	10:50	11:15
39	366781135	X	4	11:15	12:05
40	366781135	X	5	12:05	12:55
41	366781135	X	15	12:55	13:10
42	366781135	X	6	13:10	14:00
43	366781135	X	7	14:00	14:50
44	366781135	X	10	14:50	16:20
45	366781135	X	8	16:20	17:10
46	366781135	X	9	17:10	18:00
47	366781135	X	10	18:00	18:50
48	366781135	X	11	18:50	19:40
49	366781135	X	15	19:40	20:00
50	366781135	X	12	20:00	20:50
51	366781135	X	13	20:50	21:40
52	366781135	J	1	08:20	09:10
53	366781135	J	2	09:10	10:00
54	366781135	J	3	10:00	10:50
55	366781135	J	14	10:50	11:15
56	366781135	J	4	11:15	12:05
57	366781135	J	5	12:05	12:55
58	366781135	J	15	12:55	13:10
59	366781135	J	6	13:10	14:00
60	366781135	J	7	14:00	14:50
61	366781135	J	10	14:50	16:20
62	366781135	J	8	16:20	17:10
63	366781135	J	9	17:10	18:00
64	366781135	J	10	18:00	18:50
65	366781135	J	11	18:50	19:40
66	366781135	J	15	19:40	20:00
67	366781135	J	12	20:00	20:50
68	366781135	J	13	20:50	21:40
69	366781135	V	1	08:20	09:10
70	366781135	V	2	09:10	10:00
71	366781135	V	3	10:00	10:50
72	366781135	V	14	10:50	11:15
73	366781135	V	4	11:15	12:05
74	366781135	V	5	12:05	12:55
75	366781135	V	15	12:55	13:10
76	366781135	V	6	13:10	14:00
77	366781135	V	7	14:00	14:50
78	366781135	V	10	14:50	16:20
79	366781135	V	8	16:20	17:10
80	366781135	V	9	17:10	18:00
81	366781135	V	10	18:00	18:50
82	366781135	V	11	18:50	19:40
83	366781135	V	15	19:40	20:00
84	366781135	V	12	20:00	20:50
85	366781135	V	13	20:50	21:40
1	366781135	L	1	08:20	09:10
2	366781135	L	2	09:10	10:00
3	366781135	L	3	10:00	10:50
4	366781135	L	14	10:50	11:15
5	366781135	L	4	11:15	12:05
6	366781135	L	5	12:05	12:55
7	366781135	L	15	12:55	13:10
8	366781135	L	6	13:10	14:00
9	366781135	L	7	14:00	14:50
10	366781135	L	10	14:50	16:20
11	366781135	L	8	16:20	17:10
12	366781135	L	9	17:10	18:00
13	366781135	L	10	18:00	18:50
14	366781135	L	11	18:50	19:40
15	366781135	L	15	19:40	20:00
16	366781135	L	12	20:00	20:50
17	366781135	L	13	20:50	21:40
18	366781135	M	1	08:20	09:10
19	366781135	M	2	09:10	10:00
20	366781135	M	3	10:00	10:50
21	366781135	M	14	10:50	11:15
22	366781135	M	4	11:15	12:05
23	366781135	M	5	12:05	12:55
24	366781135	M	15	12:55	13:10
25	366781135	M	6	13:10	14:00
26	366781135	M	7	14:00	14:50
27	366781135	M	10	14:50	16:20
28	366781135	M	8	16:20	17:10
29	366781135	M	9	17:10	18:00
30	366781135	M	10	18:00	18:50
31	366781135	M	11	18:50	19:40
32	366781135	M	15	19:40	20:00
33	366781135	M	12	20:00	20:50
34	366781135	M	13	20:50	21:40
35	366781135	X	1	08:20	09:10
36	366781135	X	2	09:10	10:00
37	366781135	X	3	10:00	10:50
38	366781135	X	14	10:50	11:15
39	366781135	X	4	11:15	12:05
40	366781135	X	5	12:05	12:55
41	366781135	X	15	12:55	13:10
42	366781135	X	6	13:10	14:00
43	366781135	X	7	14:00	14:50
44	366781135	X	10	14:50	16:20
45	366781135	X	8	16:20	17:10
46	366781135	X	9	17:10	18:00
47	366781135	X	10	18:00	18:50
48	366781135	X	11	18:50	19:40
49	366781135	X	15	19:40	20:00
50	366781135	X	12	20:00	20:50
51	366781135	X	13	20:50	21:40
52	366781135	J	1	08:20	09:10
53	366781135	J	2	09:10	10:00
54	366781135	J	3	10:00	10:50
55	366781135	J	14	10:50	11:15
56	366781135	J	4	11:15	12:05
57	366781135	J	5	12:05	12:55
58	366781135	J	15	12:55	13:10
59	366781135	J	6	13:10	14:00
60	366781135	J	7	14:00	14:50
61	366781135	J	10	14:50	16:20
62	366781135	J	8	16:20	17:10
63	366781135	J	9	17:10	18:00
64	366781135	J	10	18:00	18:50
65	366781135	J	11	18:50	19:40
66	366781135	J	15	19:40	20:00
67	366781135	J	12	20:00	20:50
68	366781135	J	13	20:50	21:40
69	366781135	V	1	08:20	09:10
70	366781135	V	2	09:10	10:00
71	366781135	V	3	10:00	10:50
72	366781135	V	14	10:50	11:15
73	366781135	V	4	11:15	12:05
74	366781135	V	5	12:05	12:55
75	366781135	V	15	12:55	13:10
76	366781135	V	6	13:10	14:00
77	366781135	V	7	14:00	14:50
78	366781135	V	10	14:50	16:20
79	366781135	V	8	16:20	17:10
80	366781135	V	9	17:10	18:00
81	366781135	V	10	18:00	18:50
82	366781135	V	11	18:50	19:40
83	366781135	V	15	19:40	20:00
84	366781135	V	12	20:00	20:50
85	366781135	V	13	20:50	21:40
\.


--
-- TOC entry 3585 (class 0 OID 17497)
-- Dependencies: 220
-- Data for Name: aula; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.aula (id_aula, nombre) FROM stdin;
1	A00
2	A01
3	A02
4	A03
5	A04
6	A10
7	A11
8	A12
9	A13
10	A14
11	A15
12	A16
13	A17
14	A18
15	A19
16	B01
17	B02
18	B03
19	B04
20	B05
21	B11
22	B12
23	B13
24	B14
25	B15
26	B16
27	B17
28	C01
29	C11
30	C12
31	C13
32	C21
33	AULA
34	D01
35	D02
36	D03
37	D04
38	D05
39	D06
40	D11
41	D12
42	D13
43	D14
44	D15
45	PATI
46	SALAACT
\.


--
-- TOC entry 3587 (class 0 OID 17501)
-- Dependencies: 222
-- Data for Name: ausencies; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.ausencies (id_ausencia, id_docent, fecha_ausencia, is_full_day, anulada, motivo) FROM stdin;
1	2	2025-05-15	f	f	GENERAL
3	1	2025-05-16	f	f	GENERAL
3663	7	2025-05-26	f	f	GENERAL
2988	1	2025-05-26	f	f	GENERAL
3019	2	2025-05-26	f	f	GENERAL
2989	4	2025-05-26	f	f	GENERAL
2990	5	2025-05-26	f	f	GENERAL
3022	6	2025-05-26	f	f	GENERAL
39	4	2025-05-19	f	f	GENERAL
5	6	2025-05-19	f	f	GENERAL
41	5	2025-05-19	f	f	GENERAL
154	1	2025-05-20	f	f	GENERAL
155	2	2025-05-20	f	f	GENERAL
156	4	2025-05-20	f	f	GENERAL
157	5	2025-05-20	f	f	GENERAL
158	6	2025-05-20	f	f	GENERAL
514	1	2025-05-21	f	f	GENERAL
535	2	2025-05-21	f	f	GENERAL
584	4	2025-05-21	f	f	GENERAL
625	5	2025-05-21	f	f	GENERAL
504	6	2025-05-21	f	f	GENERAL
2190	1	2025-05-23	f	f	GENERAL
2239	2	2025-05-23	f	f	GENERAL
1450	1	2025-05-22	f	f	GENERAL
1332	2	2025-05-22	f	f	GENERAL
1343	4	2025-05-22	f	f	GENERAL
1364	5	2025-05-22	f	f	GENERAL
1413	6	2025-05-22	f	f	GENERAL
2280	4	2025-05-23	f	f	GENERAL
2160	5	2025-05-23	f	f	GENERAL
2171	6	2025-05-23	f	f	GENERAL
4008	1	2025-05-27	f	f	GENERAL
4100	2	2025-05-27	f	f	GENERAL
3980	4	2025-05-27	f	f	GENERAL
3991	5	2025-05-27	f	f	GENERAL
4011	6	2025-05-27	f	f	GENERAL
4369	6	2025-05-28	f	f	GENERAL
4370	1	2025-05-28	f	f	GENERAL
4371	2	2025-05-28	f	f	GENERAL
4372	4	2025-05-28	f	f	GENERAL
4373	5	2025-05-28	f	f	GENERAL
4374	2	2025-05-29	f	f	GENERAL
4375	4	2025-05-29	f	f	GENERAL
4376	5	2025-05-29	f	f	GENERAL
4377	6	2025-05-29	f	f	GENERAL
4381	5	2025-05-30	f	f	GENERAL
4382	6	2025-05-30	f	f	GENERAL
4383	1	2025-05-30	f	f	GENERAL
4384	2	2025-05-30	f	f	GENERAL
4385	4	2025-05-30	f	f	GENERAL
4386	2	2025-06-20	t	f	PERMISO
4388	2	2025-06-22	f	f	BAJA_MEDICA
4389	4	2025-06-11	t	f	OTROS
4390	4	2025-06-12	f	f	BAJA_MEDICA
4391	6	2025-06-17	f	f	OTROS
4392	2	2025-06-25	f	f	PERMISO
4393	1	2025-06-17	f	f	PERMISO
4394	4	2025-06-19	f	f	PERMISO
4395	81	2025-06-26	t	f	PERMISO
4396	1	2025-06-02	f	f	GENERAL
4397	4	2025-06-02	f	f	GENERAL
4398	5	2025-06-02	f	f	GENERAL
4399	2	2025-06-02	f	f	GENERAL
4400	6	2025-06-02	f	f	GENERAL
4401	1	2025-06-18	f	f	PERMISO
4402	4	2025-06-03	f	f	GENERAL
4403	5	2025-06-03	f	f	GENERAL
4404	6	2025-06-03	f	f	GENERAL
4405	1	2025-06-03	f	f	GENERAL
4406	2	2025-06-03	f	f	GENERAL
4407	6	2025-06-04	f	f	GENERAL
4408	1	2025-06-04	f	f	GENERAL
4409	2	2025-06-04	f	f	GENERAL
4410	4	2025-06-04	f	f	GENERAL
4411	5	2025-06-04	f	f	GENERAL
\.


--
-- TOC entry 3589 (class 0 OID 17507)
-- Dependencies: 224
-- Data for Name: ausencies_sessio; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.ausencies_sessio (id_ausencia, id_sessio) FROM stdin;
1	10
3	10
5	4
39	3
41	3
154	36
154	41
155	37
155	42
156	33
156	38
156	43
157	34
157	39
157	44
158	35
158	40
504	45
514	46
535	47
584	48
625	49
504	50
514	51
535	52
584	53
625	54
504	55
514	56
1332	57
1343	58
1364	59
1413	60
1450	61
1332	62
1343	63
1364	64
1413	65
1450	66
1332	67
1343	68
2160	69
2171	70
2190	71
2239	72
2280	73
2160	74
2171	75
2190	76
2239	77
2280	78
2160	79
2171	80
2988	3
2989	3
2990	3
3019	4
3022	4
2989	23
2990	24
3022	25
2988	26
3019	27
3022	30
2989	28
2990	29
2988	31
3019	32
3663	10
3663	999
3980	33
3991	34
4008	36
4011	35
4100	37
4008	41
4100	42
3980	38
3980	43
3991	39
3991	44
4011	40
4008	91
4100	91
3980	91
3991	91
4011	91
4369	45
4370	46
4371	47
4372	48
4373	49
4369	50
4370	51
4373	54
4371	52
4372	53
4369	55
4370	56
4369	91
4370	91
4371	91
4372	91
4373	91
4374	57
4375	58
4376	59
4377	60
4375	63
4374	60
4376	64
4377	65
4375	68
4376	91
4374	91
4375	91
4377	91
4381	69
4382	70
4383	71
4384	72
4385	73
4381	74
4382	75
4385	78
4383	76
4384	77
4381	79
4382	80
4384	91
4385	91
4381	91
4382	91
4383	91
4388	10
4388	23
4390	58
4391	35
4392	47
4392	52
4393	36
4394	63
4396	3
4397	3
4398	3
4399	4
4400	4
4397	23
4398	24
4400	25
4401	51
4401	56
4399	27
4400	30
4397	28
4398	29
4399	32
4399	91
4400	91
4397	91
4398	91
4396	91
4402	33
4403	34
4404	35
4405	36
4406	37
4402	38
4403	39
4406	42
4404	40
4405	41
4402	43
4403	44
4402	91
4406	91
4405	91
4404	91
4403	91
4407	45
4408	46
4409	47
4410	48
4411	49
4407	50
4408	51
4411	54
4409	52
4410	53
4407	55
\.


--
-- TOC entry 3590 (class 0 OID 17510)
-- Dependencies: 225
-- Data for Name: docent; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.docent (id_docent, document, nom, cognom1, cognom2, tipus_doc, sexe, data_ingres, hores_lloc, hores_dedicades, data_naix, ensenyament, organisme, username, password_hash, rol) FROM stdin;
1	12345678A	María	Pérez	García	\N	\N	\N	\N	\N	\N	\N	\N	mperez	hash1	DOCENT
2	87654321B	Luis	Gómez	Soler	\N	\N	\N	\N	\N	\N	\N	\N	lgomez	hash2	DOCENT
4	11111111A	John	Doe	\N	NIF	H	2025-05-01	20	20	1980-01-01	ESO	IES	jdoe	$2b$12$T./NmWWmlko8dxLA6E1yBOfcKlQ6UKIrXNAQnLRMH1Rk6kJdWnCN2	DOCENT
5	22222222B	Jane	Smith	\N	NIF	M	2025-05-01	20	20	1985-02-02	ESO	IES	jsmith	$2b$12$T./NmWWmlko8dxLA6E1yBOfcKlQ6UKIrXNAQnLRMH1Rk6kJdWnCN2	DOCENT
6	33333333C	Carlos	Programador	\N	NIF	H	2025-05-01	20	20	1990-03-03	FP	IES	cprogram	$2b$12$4DafNWV3koCrc55g./77.et0KuBSWNy6YGlNTAgYSKUsZI1R.mKE2	DOCENT
7	010960328X	ANGEL	A	C	\N	H	\N	\N	\N	\N	\N	\N	010960328X	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
8	010969028D	FRANCISCO JAVIER	F	C	\N	H	\N	\N	\N	\N	\N	\N	010969028D	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
9	011078949R	ANGEL	R	M	\N	H	\N	\N	\N	\N	\N	\N	011078949R	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
10	011921608R	MARCOS ANTONIO	O	Q	\N	H	\N	\N	\N	\N	\N	\N	011921608R	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
11	011939238D	JUAN ANTONIO	M	B	\N	H	\N	\N	\N	\N	\N	\N	011939238D	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
12	011949328W	JOSEP LLUIS	B	C	\N	H	\N	\N	\N	\N	\N	\N	011949328W	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
13	011973418Z	GABRIEL	C	A	\N	H	\N	\N	\N	\N	\N	\N	011973418Z	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
14	012855339X	ANGELES	B	C	\N	M	\N	\N	\N	\N	\N	\N	012855339X	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
15	012905318D	JOSE VICENTE	G	B	\N	H	\N	\N	\N	\N	\N	\N	012905318D	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
16	012950628N	PASCUAL MIGUEL	P	B	\N	H	\N	\N	\N	\N	\N	\N	012950628N	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
17	012951758W	PERE	F	G	\N	H	\N	\N	\N	\N	\N	\N	012951758W	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
18	012986535F	YOLANDA	G	M	\N	M	\N	\N	\N	\N	\N	\N	012986535F	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
19	013906018J	JOSEP ENRIC	E	A	\N	H	\N	\N	\N	\N	\N	\N	013906018J	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
20	013928518A	OLGA	S	D	\N	M	\N	\N	\N	\N	\N	\N	013928518A	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
21	013931868S	ELOY	F	R	\N	H	\N	\N	\N	\N	\N	\N	013931868S	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
22	013962048R	VICENTA	N	B	\N	M	\N	\N	\N	\N	\N	\N	013962048R	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
23	014919208N	FRANCESC	D	M	\N	H	\N	\N	\N	\N	\N	\N	014919208N	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
24	014985308L	CARME	A	H	\N	M	\N	\N	\N	\N	\N	\N	014985308L	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
25	015965468J	NURIA	C	S	\N	M	\N	\N	\N	\N	\N	\N	015965468J	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
26	016910908H	JAVIER	M	B	\N	H	\N	\N	\N	\N	\N	\N	016910908H	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
27	016946918A	MARIA NIEVES	M	F	\N	M	\N	\N	\N	\N	\N	\N	016946918A	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
28	016976848Q	ESTHER	S	O	\N	M	\N	\N	\N	\N	\N	\N	016976848Q	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
29	017050149N	MARIA JESUS	C	G	\N	M	\N	\N	\N	\N	\N	\N	017050149N	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
30	017911348P	QUEREMON	R	G	\N	H	\N	\N	\N	\N	\N	\N	017911348P	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
31	017959408Z	MARIA DE BEGOÑA	E	C	\N	M	\N	\N	\N	\N	\N	\N	017959408Z	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
32	019080189D	MONICA	G	A	\N	M	\N	\N	\N	\N	\N	\N	019080189D	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
33	019825419Z	ASENSIO	M	S	\N	H	\N	\N	\N	\N	\N	\N	019825419Z	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
34	019900748V	JESUS	A	M	\N	H	\N	\N	\N	\N	\N	\N	019900748V	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
35	020446640M	SILVIA MERCEDES	Q	I	\N	M	\N	\N	\N	\N	\N	\N	020446640M	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
36	022615771H	RUT	A	H	\N	M	\N	\N	\N	\N	\N	\N	022615771H	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
37	023440660P	JOSE	V	R	\N	H	\N	\N	\N	\N	\N	\N	023440660P	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
38	024163252T	HECTOR ANTONIO	G	A	\N	H	\N	\N	\N	\N	\N	\N	024163252T	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
39	024389784E	ISABEL	M	R	\N	M	\N	\N	\N	\N	\N	\N	024389784E	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
40	024453065H	IGNACIO	S	A	\N	H	\N	\N	\N	\N	\N	\N	024453065H	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
41	025347434A	MARIA DEL CARMEN	L	G	\N	M	\N	\N	\N	\N	\N	\N	025347434A	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
42	025470191A	SACRAMENTO	T	R	\N	M	\N	\N	\N	\N	\N	\N	025470191A	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
43	026577702D	ALEJANDRO	T	Z	\N	H	\N	\N	\N	\N	\N	\N	026577702D	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
44	026715900R	FRANK F.	W	L	\N	H	\N	\N	\N	\N	\N	\N	026715900R	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
45	027246470Z	MARIA	J	P	\N	M	\N	\N	\N	\N	\N	\N	027246470Z	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
46	028203430W	SOFIA	P	P	\N	M	\N	\N	\N	\N	\N	\N	028203430W	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
47	028301344S	BEGOÑA	F	S	\N	M	\N	\N	\N	\N	\N	\N	028301344S	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
48	028376971Q	MIGUEL	G	H	\N	H	\N	\N	\N	\N	\N	\N	028376971Q	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
49	029218450V	JORGE	R	M	\N	H	\N	\N	\N	\N	\N	\N	029218450V	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
50	031478003M	EVA	V	B	\N	M	\N	\N	\N	\N	\N	\N	031478003M	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
51	033580118G	AURORA	E	M	\N	M	\N	\N	\N	\N	\N	\N	033580118G	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
52	039660939E	JAVIER	A	E	\N	H	\N	\N	\N	\N	\N	\N	039660939E	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
53	041346258R	ANA	U	P	\N	M	\N	\N	\N	\N	\N	\N	041346258R	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
54	045760203C	NURIA	P	G	\N	M	\N	\N	\N	\N	\N	\N	045760203C	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
55	045989240B	FERRAN JOSEP	R	P	\N	H	\N	\N	\N	\N	\N	\N	045989240B	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
56	046417441L	MARIA FRANCISCA	M	A	\N	M	\N	\N	\N	\N	\N	\N	046417441L	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
57	046730283P	ALEIX	C	M	\N	H	\N	\N	\N	\N	\N	\N	046730283P	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
58	047397438M	DANIEL	M	M	\N	H	\N	\N	\N	\N	\N	\N	047397438M	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
59	047928150S	JOSE RAMON	M	G	\N	H	\N	\N	\N	\N	\N	\N	047928150S	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
60	054309783V	AGUSTIN	G	B	\N	H	\N	\N	\N	\N	\N	\N	054309783V	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
61	055742962J	SUSANA	N	P	\N	M	\N	\N	\N	\N	\N	\N	055742962J	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
62	059603111F	JOSE ANTONIO	P	O	\N	H	\N	\N	\N	\N	\N	\N	059603111F	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
63	070094824B	ANTONIO	B	P	\N	H	\N	\N	\N	\N	\N	\N	070094824B	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
64	070374673Z	SANTIAGO	M	F	\N	H	\N	\N	\N	\N	\N	\N	070374673Z	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
65	070391913Z	LIDIA	C	R	\N	M	\N	\N	\N	\N	\N	\N	070391913Z	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
66	072516423B	MARIA ISABEL	C	O	\N	M	\N	\N	\N	\N	\N	\N	072516423B	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
67	075035864L	ANTONIO	C	S	\N	H	\N	\N	\N	\N	\N	\N	075035864L	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
68	075345863D	ROSA MARIA	P	S	\N	M	\N	\N	\N	\N	\N	\N	075345863D	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
69	076304893C	DIEGO	A	P	\N	H	\N	\N	\N	\N	\N	\N	076304893C	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
70	076335803B	JOSE MIGUEL	F	P	\N	H	\N	\N	\N	\N	\N	\N	076335803B	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
71	078229283W	JOSE	O	L	\N	H	\N	\N	\N	\N	\N	\N	078229283W	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
72	078308873T	JOSE MANUEL	L	R	\N	H	\N	\N	\N	\N	\N	\N	078308873T	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
73	078395783P	M. TERESA	R	G	\N	M	\N	\N	\N	\N	\N	\N	078395783P	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
74	078399623C	JUAN	F	B	\N	H	\N	\N	\N	\N	\N	\N	078399623C	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
75	079320803C	MANUEL	S	O	\N	H	\N	\N	\N	\N	\N	\N	079320803C	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
76	079351863T	MERCEDES DEL CARMEN	J	E	\N	M	\N	\N	\N	\N	\N	\N	079351863T	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
77	X09610120P	EMILIE	B	 	\N	M	\N	\N	\N	\N	\N	\N	X09610120P	$2a$10$2bY5nE/dcOJzzg9UbxZ6qeYsYbQqsO7WxkfhUeiECz5bNKOcyYjkW	PROFESOR
78	1234	Carlos	Programador	\N	\N	\N	\N	\N	\N	\N	\N	\N	1234	1234	PROFESOR
79	87655321B	Ana	Torres	\N	\N	\N	\N	\N	\N	\N	\N	\N	87655321B	$2a$10$8eyoFIQvNp1Cs/A9USOPMOi/BvPqas9MLpzMcaxCk14Fh1ZxQpycK	PROFESOR
80	44444444Y	Juan	Cuatro	\N	\N	\N	\N	\N	\N	\N	\N	\N	44444444Y	$2a$10$1oXgL8FFzD0WWjCDI6l2M.b/Z3LtPzVdpQFrZYaGdLhbr9F60AzQ.	PROFESOR
81	00000000J	Julián	Crespo	\N	\N	\N	\N	\N	\N	\N	\N	\N	00000000J	$2a$10$4OnU7Wyta/Q/1SqbLGCCyOZsphVQpvmIyLCvmP895rdnNpRK99Scq	PROFESOR
82	56789210P	Profe	Pruebas	\N	\N	\N	\N	\N	\N	\N	\N	\N	56789210P	$2a$10$rrOwfgd/LZnyP8Cq6RJrcu3N/5c150Num0J.nb9CGlZn8gFpNPpba	PROFESOR
83	12345678S	Salermo	Juanes	\N	\N	\N	\N	\N	\N	\N	\N	\N	12345678S	$2a$10$HiSn/qiHaPGIXM1iIp57Tu4LBQTXl/ipixqm/RODNdFspg5xC7biC	PROFESOR
\.


--
-- TOC entry 3592 (class 0 OID 17516)
-- Dependencies: 227
-- Data for Name: docent_sessio; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.docent_sessio (id_asignacion, id_docent, id_sessio, ocupacion) FROM stdin;
100	1	10	Titular
101	2	20	Titular
102	4	3	Inglés
103	5	3	Inglés (aux)
104	6	4	Programación
108	1	3	CLASE
109	2	4	CLASE
110	4	23	CLASE
111	5	24	CLASE
112	6	25	CLASE
113	1	26	CLASE
114	2	27	CLASE
115	4	28	CLASE
116	5	29	CLASE
117	6	30	CLASE
118	1	31	CLASE
119	2	32	CLASE
120	4	33	CLASE
121	5	34	CLASE
122	6	35	CLASE
123	1	36	CLASE
124	2	37	CLASE
125	4	38	CLASE
126	5	39	CLASE
127	6	40	CLASE
128	1	41	CLASE
129	2	42	CLASE
130	4	43	CLASE
131	5	44	CLASE
132	6	45	CLASE
133	1	46	CLASE
134	2	47	CLASE
135	4	48	CLASE
136	5	49	CLASE
137	6	50	CLASE
138	1	51	CLASE
139	2	52	CLASE
140	4	53	CLASE
141	5	54	CLASE
142	6	55	CLASE
143	1	56	CLASE
144	2	57	CLASE
145	4	58	CLASE
146	5	59	CLASE
147	6	60	CLASE
148	1	61	CLASE
149	2	62	CLASE
150	4	63	CLASE
151	5	64	CLASE
152	6	65	CLASE
153	1	66	CLASE
154	2	67	CLASE
155	4	68	CLASE
156	5	69	CLASE
157	6	70	CLASE
158	1	71	CLASE
159	2	72	CLASE
160	4	73	CLASE
161	5	74	CLASE
162	6	75	CLASE
163	1	76	CLASE
164	2	77	CLASE
165	4	78	CLASE
166	5	79	CLASE
167	6	80	CLASE
\.


--
-- TOC entry 3594 (class 0 OID 17520)
-- Dependencies: 229
-- Data for Name: fichaje; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.fichaje (id, dni, fecha_hora, tipo) FROM stdin;
3	admin	2025-04-24 15:43:15.119087	IN
4	admin	2025-04-24 18:42:16.843641	IN
5	admin	2025-04-24 18:42:27.4252	IN
6	admin	2025-04-24 18:42:29.441233	OUT
7	admin	2025-04-24 18:44:42.923786	IN
8	admin	2025-05-05 09:20:50.991213	IN
9	admin	2025-05-05 09:20:54.028679	OUT
10	admin	2025-05-05 09:23:32.499548	IN
11	admin	2025-05-05 09:23:33.322172	OUT
12	admin	2025-05-05 09:51:14.348285	IN
13	admin	2025-05-05 09:51:15.105701	OUT
14	admin	2025-05-05 09:51:15.805938	OUT
15	admin	2025-05-05 09:51:15.976459	OUT
16	admin	2025-05-05 09:51:16.146996	OUT
17	admin	2025-05-05 09:51:16.325717	OUT
18	admin	2025-05-05 09:51:16.466703	OUT
19	admin	2025-05-05 09:51:16.604358	OUT
20	admin	2025-05-05 09:51:16.827896	OUT
21	admin	2025-05-05 09:51:17.016931	OUT
22	admin	2025-05-05 09:51:17.169365	OUT
23	admin	2025-05-05 09:51:17.303213	OUT
24	admin	2025-05-05 09:51:17.45451	OUT
25	admin	2025-05-05 09:51:17.575542	OUT
26	admin	2025-05-05 09:51:17.694707	OUT
27	admin	2025-05-05 09:51:17.814379	OUT
28	admin	2025-05-05 09:51:17.934393	OUT
29	admin	2025-05-05 09:51:18.054409	OUT
30	admin	2025-05-06 13:16:58.026057	IN
31	admin	2025-05-06 13:17:00.766471	OUT
\.


--
-- TOC entry 3596 (class 0 OID 17525)
-- Dependencies: 231
-- Data for Name: fichajes; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.fichajes (id, fecha, hora_fin, hora_inicio, usuario_id, total) FROM stdin;
1	2025-05-09	00:22:23.435	00:22:21.239	admin	00:00:00
2	2025-05-10	09:45:05.157	09:45:01.552	admin	00:00:00
4	2025-05-10	12:12:18.003	12:11:46.45	admin	00:00:00
5	2025-05-10	12:41:23.831	12:40:46.422	admin	00:00:00
6	2025-05-10	12:58:17.015	12:57:59.704	admin	00:00:00
7	2025-05-10	12:58:45.303	12:58:37.026	admin	00:00:00
8	2025-05-10	13:01:27	13:01:22	admin	00:01:44
34	2025-05-20	17:36:41	17:35:58	admin	00:00:43
9	2025-05-10	12:27:59	12:27:47	admin	00:01:56
35	2025-05-20	\N	18:00:21	admin	00:00:00
10	2025-05-10	17:16:07	17:15:20	admin	00:02:43
11	2025-05-12	20:12:24	20:09:59	admin	00:02:25
12	2025-05-12	20:31:20	20:12:28	admin	00:21:17
36	2025-05-21	19:00:45	19:00:40	1234	00:00:05
13	2025-05-12	20:34:46	20:31:22	admin	00:24:41
37	2025-05-21	\N	19:11:24	admin	00:00:00
14	2025-05-12	20:42:18	20:42:13	admin	00:24:46
15	2025-05-12	21:00:29	20:58:06	admin	00:27:09
16	2025-05-12	21:13:34	21:13:03	admin	00:27:40
38	2025-05-21	22:55:42	22:55:39	1234	00:00:08
17	2025-05-12	21:23:10	21:17:34	admin	00:33:16
18	2025-05-13	10:24:44	10:24:39	admin	00:00:05
19	2025-05-13	11:02:38	10:24:52	admin	00:37:51
39	2025-05-22	12:04:55	07:53:53	admin	04:11:02
20	2025-05-13	11:04:10	11:03:54	admin	00:38:07
40	2025-05-22	\N	12:04:57	admin	00:00:00
21	2025-05-13	11:20:27	11:20:10	admin	00:38:24
22	2025-05-13	\N	11:20:32	admin	00:00:00
23	2025-05-14	21:25:10	21:25:08	admin	00:00:02
24	2025-05-14	21:25:17	21:25:14	admin	00:00:05
41	2025-05-26	10:24:42	10:15:57	1234	00:08:45
25	2025-05-14	21:27:46	21:25:19	admin	00:02:32
26	2025-05-15	20:33:07	19:31:01	admin	01:02:06
27	2025-05-15	\N	20:43:46	admin	00:00:00
28	2025-05-16	11:20:23	11:08:09	admin	00:12:14
42	2025-05-26	10:28:47	10:25:11	admin	00:03:36
29	2025-05-16	14:46:35	14:46:28	admin	00:12:21
30	2025-05-16	18:09:27	18:09:18	admin	00:12:30
31	2025-05-19	08:24:12	08:24:10	admin	00:00:02
43	2025-05-26	13:51:09	13:51:06	1234	00:08:48
32	2025-05-19	19:28:30	19:28:26	1234	00:00:04
33	2025-05-19	19:34:21	19:28:49	1234	00:05:36
44	2025-05-26	14:20:55	14:20:53	admin	00:03:38
45	2025-05-26	\N	16:16:13	admin	00:00:00
46	2025-05-26	\N	16:22:00	1234	00:00:00
47	2025-05-27	\N	09:44:37	1234	00:00:00
48	2025-05-28	\N	13:52:01	1234	00:00:00
49	2025-05-28	18:06:33	18:06:22	admin	00:00:11
50	2025-05-29	\N	09:34:01	12345678A	00:00:00
51	2025-05-29	\N	12:00:00	12345678A	00:00:00
53	2025-05-29	\N	09:33:21.972509	12345678A	00:00:00
54	2025-05-29	\N	10:02:31	87654321B	00:00:00
55	2025-06-01	\N	00:02:46	1234	00:00:00
56	2025-06-02	09:01:46	09:01:42	12345678A	00:00:04
57	2025-06-02	\N	09:01:47	12345678A	00:00:00
58	2025-06-02	\N	09:09:16	admin	00:00:00
59	2025-06-02	19:05:27	19:03:30	1234	00:01:57
60	2025-06-02	19:08:26	19:08:22	1234	00:02:01
61	2025-06-02	\N	19:08:41	1234	00:00:00
62	2025-06-04	19:12:37	19:12:34	1234	00:00:03
63	2025-06-04	19:19:24	19:15:49	1234	00:03:38
64	2025-06-04	19:19:34	19:19:29	1234	00:03:43
65	2025-06-04	19:23:51	19:19:38	1234	00:07:56
\.


--
-- TOC entry 3598 (class 0 OID 17530)
-- Dependencies: 233
-- Data for Name: grupo; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.grupo (id_grupo, nombre, etapa, curso) FROM stdin;
1	1º ESO A	ESO	1ESO
2	1º ESO B	ESO	1ESO
3	2º ESO A	ESO	2ESO
4	2º ESO B	ESO	2ESO
5	3º ESO A	ESO	3ESO
\.


--
-- TOC entry 3600 (class 0 OID 17534)
-- Dependencies: 235
-- Data for Name: guardies; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.guardies (id_guardia, docent_assignat, docent_absent, id_sessio, fecha_guardia) FROM stdin;
1	1	2	10	2025-05-15
3	\N	1	36	2025-05-20
4	\N	1	41	2025-05-20
6	\N	2	42	2025-05-20
7	\N	4	33	2025-05-20
8	\N	4	38	2025-05-20
9	\N	4	43	2025-05-20
10	\N	5	34	2025-05-20
11	\N	5	39	2025-05-20
12	\N	5	44	2025-05-20
13	\N	6	35	2025-05-20
14	\N	6	40	2025-05-20
5	4	2	37	2025-05-20
839	\N	2	47	2025-05-21
888	\N	4	48	2025-05-21
929	\N	5	49	2025-05-21
981	\N	6	50	2025-05-21
1145	\N	1	51	2025-05-21
1217	\N	2	52	2025-05-21
1299	\N	4	53	2025-05-21
1391	\N	5	54	2025-05-21
1494	\N	6	55	2025-05-21
1585	\N	1	56	2025-05-21
2136	\N	6	45	2025-05-21
2137	\N	1	46	2025-05-21
2246	\N	2	57	2025-05-22
2257	\N	4	58	2025-05-22
2278	\N	5	59	2025-05-22
2327	\N	6	60	2025-05-22
2364	\N	1	61	2025-05-22
2416	\N	2	62	2025-05-22
2586	\N	4	63	2025-05-22
2658	\N	5	64	2025-05-22
2740	\N	6	65	2025-05-22
2823	\N	1	66	2025-05-22
2926	\N	2	67	2025-05-22
3039	\N	4	68	2025-05-22
3680	\N	5	69	2025-05-23
3691	\N	6	70	2025-05-23
3710	\N	1	71	2025-05-23
3759	\N	2	72	2025-05-23
3800	\N	4	73	2025-05-23
3852	\N	5	74	2025-05-23
4022	\N	6	75	2025-05-23
4087	\N	1	76	2025-05-23
4169	\N	2	77	2025-05-23
4261	\N	4	78	2025-05-23
4364	\N	5	79	2025-05-23
4477	\N	6	80	2025-05-23
5114	\N	1	3	2025-05-26
5145	\N	2	4	2025-05-26
5192	\N	4	23	2025-05-26
5248	\N	5	24	2025-05-26
5250	\N	6	25	2025-05-26
5468	\N	1	26	2025-05-26
5587	\N	2	27	2025-05-26
5694	\N	6	30	2025-05-26
5734	\N	4	28	2025-05-26
5881	\N	5	29	2025-05-26
6238	\N	1	31	2025-05-26
6241	\N	2	32	2025-05-26
6476	\N	7	10	2025-05-26
6525	\N	7	999	2025-05-26
7544	\N	4	33	2025-05-27
7555	\N	5	34	2025-05-27
7572	\N	1	36	2025-05-27
7575	\N	6	35	2025-05-27
7684	\N	2	37	2025-05-27
7719	\N	2	42	2025-05-27
7721	\N	4	38	2025-05-27
7724	\N	5	39	2025-05-27
7725	78	5	44	2025-05-27
7722	78	4	43	2025-05-27
7717	78	1	41	2025-05-27
7727	78	6	40	2025-05-27
8460	\N	1	91	2025-05-27
8467	78	1	51	2025-05-28
8469	78	5	54	2025-05-28
8470	78	6	50	2025-05-28
8471	78	5	49	2025-05-28
8472	78	4	48	2025-05-28
8473	78	2	52	2025-05-28
8474	78	6	45	2025-05-28
8475	78	4	53	2025-05-28
8476	78	6	55	2025-05-28
8477	78	1	91	2025-05-28
8483	2	2	60	2025-05-29
8505	78	5	64	2025-05-29
8509	2	4	68	2025-05-29
8519	78	5	69	2025-05-30
8522	78	6	75	2025-05-30
8523	78	6	80	2025-05-30
8524	78	6	25	2025-06-02
8532	78	2	37	2025-06-03
8534	78	4	38	2025-06-03
8536	78	2	47	2025-06-04
8537	78	5	49	2025-06-04
8538	78	6	55	2025-06-04
\.


--
-- TOC entry 3602 (class 0 OID 17538)
-- Dependencies: 237
-- Data for Name: sessions_horari; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.sessions_horari (id_sessio, plantilla, dia_setmana, sessio_ordre, hora_desde, hora_fins, id_aula, id_grupo) FROM stdin;
57	366781135	Jueves	1	08:20:00	09:10:00	2	1
58	366781135	Jueves	2	09:10:00	10:00:00	3	2
46	366781135	Miércoles	2	09:10:00	10:00:00	22	4
3	366781135	Lunes	1	08:20:00	09:10:00	11	5
31	366781135	Lunes	11	18:50:00	19:40:00	21	1
40	366781135	Martes	8	16:20:00	17:10:00	27	4
41	366781135	Martes	9	17:10:00	18:00:00	32	1
32	366781135	Lunes	12	20:00:00	20:50:00	13	4
47	366781135	Miércoles	3	10:00:00	10:50:00	25	2
28	366781135	Lunes	8	16:20:00	17:10:00	4	3
88	366781135	Lunes	15	12:55:00	13:10:00	26	3
30	366781135	Lunes	10	14:50:00	16:20:00	2	1
38	366781135	Martes	6	13:10:00	14:00:00	45	1
42	366781135	Martes	10	14:50:00	16:20:00	13	5
44	366781135	Martes	12	20:00:00	20:50:00	24	2
55	366781135	Miércoles	11	18:50:00	19:40:00	19	1
49	366781135	Miércoles	5	12:05:00	12:55:00	11	5
34	366781135	Martes	2	09:10:00	10:00:00	3	2
35	366781135	Martes	3	10:00:00	10:50:00	5	4
89	366781135	Lunes	13	20:50:00	21:40:00	29	1
91	366781135	Martes	13	20:50:00	21:40:00	18	3
67	366781135	Jueves	11	18:50:00	19:40:00	21	4
56	366781135	Miércoles	12	20:00:00	20:50:00	26	5
50	366781135	Miércoles	6	13:10:00	14:00:00	17	3
68	366781135	Jueves	12	20:00:00	20:50:00	28	3
51	366781135	Miércoles	7	14:00:00	14:50:00	36	4
54	366781135	Miércoles	10	14:50:00	16:20:00	5	3
81	366781135	Jueves	13	20:50:00	21:40:00	12	1
95	366781135	Lunes	14	10:50:00	11:15:00	18	5
53	366781135	Miércoles	9	17:10:00	18:00:00	14	2
84	366781135	Jueves	15	12:55:00	13:10:00	17	4
86	366781135	Miércoles	15	12:55:00	13:10:00	7	2
87	366781135	Miércoles	13	20:50:00	21:40:00	9	3
48	366781135	Miércoles	4	11:15:00	12:05:00	6	5
93	366781135	Jueves	14	10:50:00	11:15:00	31	5
83	366781135	Viernes	15	12:55:00	13:10:00	10	3
65	366781135	Jueves	9	17:10:00	18:00:00	6	5
52	366781135	Miércoles	8	16:20:00	17:10:00	4	3
66	366781135	Jueves	10	14:50:00	16:20:00	15	2
23	366781135	Lunes	3	10:00:00	10:50:00	23	1
74	366781135	Viernes	6	13:10:00	14:00:00	10	3
72	366781135	Viernes	4	11:15:00	12:05:00	20	2
92	366781135	Viernes	14	10:50:00	11:15:00	11	4
75	366781135	Viernes	7	14:00:00	14:50:00	25	1
24	366781135	Lunes	4	11:15:00	12:05:00	30	2
59	366781135	Jueves	3	10:00:00	10:50:00	5	4
60	366781135	Jueves	4	11:15:00	12:05:00	6	5
61	366781135	Jueves	5	12:05:00	12:55:00	2	1
69	366781135	Viernes	1	08:20:00	09:10:00	38	1
63	366781135	Jueves	7	14:00:00	14:50:00	4	1
90	366781135	Miércoles	14	10:50:00	11:15:00	29	2
999	Test	V	\N	21:00:00	23:59:00	8	3
73	366781135	Viernes	5	12:05:00	12:55:00	23	5
76	366781135	Viernes	8	16:20:00	17:10:00	43	3
77	366781135	Viernes	9	17:10:00	18:00:00	9	4
79	366781135	Viernes	11	18:50:00	19:40:00	18	2
80	366781135	Viernes	12	20:00:00	20:50:00	8	1
25	366781135	Lunes	5	12:05:00	12:55:00	12	4
70	366781135	Viernes	2	09:10:00	10:00:00	3	2
71	366781135	Viernes	3	10:00:00	10:50:00	5	4
20	PL1	L	2	09:10:00	10:00:00	14	1
82	366781135	Viernes	13	20:50:00	21:40:00	9	2
2	TEST	Lunes	2	09:10:00	10:00:00	19	2
26	366781135	Lunes	6	13:10:00	14:00:00	17	3
27	366781135	Lunes	7	14:00:00	14:50:00	7	5
78	366781135	Viernes	10	14:50:00	16:20:00	2	1
29	366781135	Lunes	9	17:10:00	18:00:00	16	2
1	TEST	Lunes	1	08:20:00	09:10:00	17	5
62	366781135	Jueves	6	13:10:00	14:00:00	23	5
85	366781135	Martes	14	10:50:00	11:15:00	11	4
4	366781135	Lunes	2	09:10:00	10:00:00	11	2
10	PL1	L	1	08:20:00	09:10:00	14	1
64	366781135	Jueves	8	16:20:00	17:10:00	35	2
43	366781135	Martes	11	18:50:00	19:40:00	17	4
36	366781135	Martes	4	11:15:00	12:05:00	22	5
33	366781135	Martes	1	08:20:00	09:10:00	10	5
37	366781135	Martes	5	12:05:00	12:55:00	9	3
45	366781135	Miércoles	1	08:20:00	09:10:00	20	1
39	366781135	Martes	7	14:00:00	14:50:00	15	2
94	366781135	Martes	15	12:55:00	13:10:00	5	4
\.


--
-- TOC entry 3604 (class 0 OID 17542)
-- Dependencies: 239
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: magiuser
--

COPY public.usuarios (dni, nombre, password, rol) FROM stdin;
11111111A	John Doe	$2b$12$T./NmWWmlko8dxLA6E1yBOfcKlQ6UKIrXNAQnLRMH1Rk6kJdWnCN2	PROFESOR
22222222B	Jane Smith	$2b$12$T./NmWWmlko8dxLA6E1yBOfcKlQ6UKIrXNAQnLRMH1Rk6kJdWnCN2	PROFESOR
56789210P	Profe Pruebas	$2a$10$rrOwfgd/LZnyP8Cq6RJrcu3N/5c150Num0J.nb9CGlZn8gFpNPpba	PROFESOR
011949328W	JOSEP LLUIS B	$2a$10$Ex/7qJ/29MHioCkD8Jy/OuKLtjE.IIi57zsjJ/aFH6Td5Yqf65.se	DOCENT
87655321B	Ana Torres	$2a$10$8eyoFIQvNp1Cs/A9USOPMOi/BvPqas9MLpzMcaxCk14Fh1ZxQpycK	PROFESOR
44444444Y	Juan Cuatro	$2a$10$1oXgL8FFzD0WWjCDI6l2M.b/Z3LtPzVdpQFrZYaGdLhbr9F60AzQ.	PROFESOR
00000000J	Julián Crespo	$2a$10$4OnU7Wyta/Q/1SqbLGCCyOZsphVQpvmIyLCvmP895rdnNpRK99Scq	PROFESOR
1234	Carlos Programador	$2a$10$iy1sZpUQUNBv2hQ7R5Haj.Kony0W/GSAk4kdyzKq8PUcXTRiX2noC	PROFESOR
admin	Admin	$2a$10$.QIREvPLp506LSQSpYj4VOq8UslS9X9HCj.iM3r7U0f3x0oTbmYru	ADMIN
33333333C	Carlos Programador	$2a$10$XTxgNN4jpLa4St2eC1Oo4uT8.s2qo3UelMfpANKy.cOGVL4O00tCG	DOCENT
010960328X	ANGEL A	$2a$10$zXYXm0x5PWVgoPdvBso8TOKJ8.G7VEKQDhh1YPJGyWTqdyqTe3rw.	DOCENT
010969028D	FRANCISCO JAVIER F	$2a$10$Q5fmMiosYjpkAF2ZEnf3P.YRIhRDrkJnkYeJTnroCpJlQH0V2Xrxa	DOCENT
011078949R	ANGEL R	$2a$10$a5oU9NCMLDHjhPDBdG0yre5UK56FFTu6Upnkq3ZAJOeM8TOHwVK9O	DOCENT
011921608R	MARCOS ANTONIO O	$2a$10$CCWXeabvgLSfUCU2XzK9aOTy98tlGnHvuvB9Jtpe2Hf7Vn.3YPUwi	DOCENT
011939238D	JUAN ANTONIO M	$2a$10$7FEKa2DECwMX96j3ad8MJ.Z00SR6ZoHIUyNlNnpJ.SiyDFAH/N1Ki	DOCENT
011973418Z	GABRIEL C	$2a$10$yq5S7pq4r0mBn33WJNt5C.Ydwgyr.NNKw99PgTFLhJ6LgORHUSxci	DOCENT
012855339X	ANGELES B	$2a$10$xctH2wVLLlelIxRyNmTzBeG/kG4L6NyNdlSoAt/BTaoqdnlYKi4f.	DOCENT
012905318D	JOSE VICENTE G	$2a$10$/gCwnwXiXTPHGSPxjS4qQupR6W.oRdsMOYI3hkI4kzeNeWe.ogYMq	DOCENT
012950628N	PASCUAL MIGUEL P	$2a$10$beq7CYDO6JhTri9LHJPS8es2p6KisdujbNpfWSaRGKi6xqYNk.EmW	DOCENT
012951758W	PERE F	$2a$10$xCULCHqrXAEKSf7c4Z3HW.WardmOFCwShg8ZD6x2QbfqMQJiiHeHy	DOCENT
012986535F	YOLANDA G	$2a$10$Qh8bUB/QUgJc48IP5I9MVOiKP0tLtjQdajZtPwlOJE8NOxAQtb59W	DOCENT
013906018J	JOSEP ENRIC E	$2a$10$T.CHSOPLrnFhLFbXqN9aCOQN6zivdej2DqKhlKbGMNKRySdS9qX66	DOCENT
013928518A	OLGA S	$2a$10$.0ZjwjWtDrVkPGesrD4G8u4xfxz6AZD5tLAbOzEJiSkEXFfDC59.u	DOCENT
013931868S	ELOY F	$2a$10$/kC/eLKlpCsWj2h5cwNh/.eIWYvuhgH17feb8Iq72ihBxX/s7ARNW	DOCENT
013962048R	VICENTA N	$2a$10$e1kY/4iwnsO5LQeq0rxMzOmSz5dGWkeyXVS9eLsT5e88yKxaowAb6	DOCENT
014919208N	FRANCESC D	$2a$10$ucFsUhw6s/fjDwLiVtpEZuxqf1u220MLVc2pOz2SCKSojuFqnq1Ba	DOCENT
014985308L	CARME A	$2a$10$ry5DX32ael1SkrFqPBeOtObgeaaWR9oBLhCLq1G8I/ziWdCCwTqr2	DOCENT
015965468J	NURIA C	$2a$10$NKoRp7qYbX8VQSuXYXxB5.81UrqI511mHlxg3o6tWzgkchCKmqfme	DOCENT
016910908H	JAVIER M	$2a$10$ulj2prdHvXIUaORYc4VSleOwNcw731U0RcrHuqzG/VQhAZKjtyd2W	DOCENT
016946918A	MARIA NIEVES M	$2a$10$XrVT5IKfUT1qK12q3YwEU.uWxKMaZkeN0J5KS5UuSyhBTvDQqhr0K	DOCENT
016976848Q	ESTHER S	$2a$10$Y3rEPfXTcospcPd1I9HDp.MLsWdsgWJQlGcsabzZIu3gRJXt9DB2m	DOCENT
017050149N	MARIA JESUS C	$2a$10$9ErFr3rRLm1Ugxz6UK98AOq5xBJiVpZO6SU5ZK1y/umwsP9XTuSSe	DOCENT
017911348P	QUEREMON R	$2a$10$.KiNE6sAQw/YWaxZ0Eb0EuKrZAAdKlcdyjwqdvIFXItYyKZY8/QrO	DOCENT
017959408Z	MARIA DE BEGOÑA E	$2a$10$NSRRFzmUHrHOSofjT4tJkOuwQqL.qdRT7egaSZ.fqjoEWMkEIsNC2	DOCENT
019080189D	MONICA G	$2a$10$jnwE/gL9V3W.qkDvA9X6sOTZ2r9cfZZCTUwK3XLzD.Tbp2nWvRxTi	DOCENT
019825419Z	ASENSIO M	$2a$10$YRtEROvi3On8zD3qz72QduEMFOxOZDLLnHNs0KwKSM3OnaYSXko7y	DOCENT
019900748V	JESUS A	$2a$10$RnJeRqU3YXtat251hgpszu5RPFBLn6NXmcD1xRuVLKmqVoOPJRlVu	DOCENT
020446640M	SILVIA MERCEDES Q	$2a$10$sJW1TRCJ.QjFciENjnZQouY7xKzl.PsmP7rsr486RXFqI4lIxc2/S	DOCENT
022615771H	RUT A	$2a$10$4LC1WkfCjg4NOXXrDNdGXexJbv7UV2Ro096pTQH.ctScx3RU7I/.S	DOCENT
023440660P	JOSE V	$2a$10$BP8tkpr5W28k3pCpEOiIguI.4rFlj0hjcdSwHs.Cx7wtAQxWVz68u	DOCENT
024163252T	HECTOR ANTONIO G	$2a$10$pbZBouvJlvQxP.15A89lLulwERtoYUZHs8mcooS90F8mBLCYv.mqi	DOCENT
024389784E	ISABEL M	$2a$10$foo7r6lztVhtAh5.6Jz3SuHSPboBa/voujw6AP37m8lfTsVF6Eq8O	DOCENT
024453065H	IGNACIO S	$2a$10$2sCgr0v4lSFjLbvcMTi5k.9W2FYngZwxpx46VJ8.7epVzEjCYW.Yy	DOCENT
025347434A	MARIA DEL CARMEN L	$2a$10$GR4PEl16KRaNk0V6v8oNrOAcuKlq8wzigcjGGtI9e2gDjFY5dN0AG	DOCENT
025470191A	SACRAMENTO T	$2a$10$KTbSq5ysRMfmjzdaNdOUvuI7IeExhmLjyTt.qddx30rs5Vq0t/5Ly	DOCENT
026577702D	ALEJANDRO T	$2a$10$34yvaLOOS/cmPn/KSfy4JOOk1f6B3BCgR596nNOf49AX47BwFzsGm	DOCENT
026715900R	FRANK F. W	$2a$10$umnxMyDfNzW50m14KMW2peqZd/dT6hThFBu1IkB3zqRm/UKy23mwi	DOCENT
027246470Z	MARIA J	$2a$10$RTqbVvkj2Ko6gXg2DhLVwOWQhh2lHGzQN5xSsSRLGTKZVKjpMB3/C	DOCENT
028203430W	SOFIA P	$2a$10$iwm2vsDQllKCgX0H5UYhz.SNNNT7pInqTsWwibo1HnJ5nZNSFiNJC	DOCENT
028301344S	BEGOÑA F	$2a$10$t98U2NZxEdMx8WaOySdH9.cHV4hFoaqze9Bi47q4B0NVPdkrzON0i	DOCENT
028376971Q	MIGUEL G	$2a$10$etUVOuBFJJZyhTZ8qaxdnux/DH6tX6PNoI0.DNA9H7OSZXuZiEwtK	DOCENT
029218450V	JORGE R	$2a$10$./XFtpoD4G0vFhj3wOwJ9ensGLU07sBRy/EceTFTTKLmfCFcOrA.e	DOCENT
031478003M	EVA V	$2a$10$RBmQ6bbI7w.ZtjHxxvF5wOJmc2ws7h2L.sapXFU3yNQHwkNsilmXu	DOCENT
033580118G	AURORA E	$2a$10$jgM5GxDm2y3AAtl/F16wK.ZfGp4elQLnCbyrQhQbhadrB705LHd6W	DOCENT
039660939E	JAVIER A	$2a$10$qteBLvMWDbElFqRDU6Jlne.0lleeAex42Lsd7SuB7ez82XjtbZsnK	DOCENT
041346258R	ANA U	$2a$10$kXuP4.MuQRcJuxkOSfG/1.JMgZlzE3zM/JDsG7OloxbbGGpt422W.	DOCENT
045760203C	NURIA P	$2a$10$hYMCqM4nU7ceSlUFnAe5Aeb0RJaBtE6hjAjcm96ZFA0.BsXjxmtxq	DOCENT
045989240B	FERRAN JOSEP R	$2a$10$gOV28btoBQV7YJwOfeZJ0ufkawiuow9fafyqJXz8BOvLL/dtLrFGO	DOCENT
046417441L	MARIA FRANCISCA M	$2a$10$CLa6Qniyngkvn98Hbiw0YOgzBh9x/BZafmKMFRx.XUamxEzxRJbV.	DOCENT
046730283P	ALEIX C	$2a$10$ghOSSSY0XYyrHubVxqrHlOliuBgazB4UZBi0kza7kxiG/3RgG3fDO	DOCENT
047397438M	DANIEL M	$2a$10$/xkjtYOnUy3zvs.72BVkueoABjT76DZwxCFey.g6WzS4WwCMqrNzS	DOCENT
047928150S	JOSE RAMON M	$2a$10$ii2n1U9XrsFnxH2efwVWAuF3QV7MqS6slSIj7SJ57k1r1dU2IbDIe	DOCENT
054309783V	AGUSTIN G	$2a$10$bMls5QCvtF.wzHS92JwZwulDciC1StNA17YAeZ1ljcVnLRBXJM6Aa	DOCENT
055742962J	SUSANA N	$2a$10$PNRdcPC.vGrHsg6Q.5z/t.93FZ/HU67qx5oSN2ljlCibuI0z2Fdrm	DOCENT
059603111F	JOSE ANTONIO P	$2a$10$C6SU1IC6C5VNHjpF3VHVUe1HNP.l/Kukb6EhPomoascfTMJkeB0zK	DOCENT
070094824B	ANTONIO B	$2a$10$D96fSAlRUNb7CMuJSCtrU.gm0i8b.kqtWH1edYpVg4/uDCsre8GmW	DOCENT
070374673Z	SANTIAGO M	$2a$10$RIUKzLYs7k/CGNTFr5UOcuE1eaViJwsseTzaBrPzuIMJenAmQIJrm	DOCENT
070391913Z	LIDIA C	$2a$10$vTPwZsOu7hcFH0iygeEohO/rTYgTmBIDvdfMHfwXtPClyq/rFrpIy	DOCENT
072516423B	MARIA ISABEL C	$2a$10$Tv95SYgxKG/g/YoR5B.rMOhhmwXD773VYFBBEBZMkxWzn7uypemmS	DOCENT
075035864L	ANTONIO C	$2a$10$xLGujjqnzpXCnqsD8e/Pk.BAXKiyzaYUi4Q1f1FC792cPV9m114oe	DOCENT
075345863D	ROSA MARIA P	$2a$10$jBrUoV4ARpgxLc9z24.sgem86ky0peXPpbBJpPa0pPiyQ9ZSDXPq.	DOCENT
076304893C	DIEGO A	$2a$10$Qe1SU6tbZO1rD4Xc63CjsuxsDFmAz2nqtwZy.23nwzL5uEgsl2cU2	DOCENT
076335803B	JOSE MIGUEL F	$2a$10$6x7.1becUsiL9E9KmDRWIeERRMJnvzWi3DrkjqjN9HoENd/TOnAlO	DOCENT
078229283W	JOSE O	$2a$10$PNtEtjDeplMORRgCgZWcc.nQ72XlNvlDPJxQoiNCcI6Kz6rJyrarG	DOCENT
078308873T	JOSE MANUEL L	$2a$10$PShH5e0lhLXIwX/BfYyoSe8fT56gCrVT59RsSsXuS2QJEMkxounKm	DOCENT
078395783P	M. TERESA R	$2a$10$Df.Olp1QjR7O182M4ekNoeaQ0oZnA3XNjBNiNNANBNsqDHhD1Zx0q	DOCENT
078399623C	JUAN F	$2a$10$QlgxBPPTGJK9ozt4UFTkUes0GX7SmI1nag3eYGq6yaX92E9T6DF2m	DOCENT
079320803C	MANUEL S	$2a$10$8uo4WJ2sxVBPWtLmYTyML.jJOmZUt40r13xV3tc1yGfsCXJVkjPYm	DOCENT
079351863T	MERCEDES DEL CARMEN J	$2a$10$1wIyMV7lK/LgMBxXqf7kj.dMqVUwkYNEAiuDlc2WVoD/7p9zjLB6i	DOCENT
X09610120P	EMILIE B	$2a$10$Bn6I88W5DQ6Q7mzOU0ObY.TXtIhXH4mVFNLnY8VPnQHlprXj1sObi	DOCENT
12345678A	María Pérez	$2a$10$d3YyORcil8BnkaOr/fKqiuAlWHnEiE05cYxVFLip01pBh8sZ3hQyW	DOCENT
87654321B	Luis Gómez	$2a$10$RkNFitIbL1DtqRVCfnePEuaDPZ8d0UMyRPW2uyAaOZnOUkhvXs2jC	DOCENT
12345678S	Salermo Juanes	$2a$10$HiSn/qiHaPGIXM1iIp57Tu4LBQTXl/ipixqm/RODNdFspg5xC7biC	PROFESOR
\.


--
-- TOC entry 3621 (class 0 OID 0)
-- Dependencies: 221
-- Name: aula_id_aula_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.aula_id_aula_seq', 46, true);


--
-- TOC entry 3622 (class 0 OID 0)
-- Dependencies: 223
-- Name: ausencies_id_ausencia_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.ausencies_id_ausencia_seq', 4411, true);


--
-- TOC entry 3623 (class 0 OID 0)
-- Dependencies: 226
-- Name: docent_id_docent_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.docent_id_docent_seq', 83, true);


--
-- TOC entry 3624 (class 0 OID 0)
-- Dependencies: 228
-- Name: docent_sessio_id_asignacion_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.docent_sessio_id_asignacion_seq', 287, true);


--
-- TOC entry 3625 (class 0 OID 0)
-- Dependencies: 230
-- Name: fichaje_id_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.fichaje_id_seq', 31, true);


--
-- TOC entry 3626 (class 0 OID 0)
-- Dependencies: 232
-- Name: fichajes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.fichajes_id_seq', 65, true);


--
-- TOC entry 3627 (class 0 OID 0)
-- Dependencies: 234
-- Name: grupo_id_grupo_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.grupo_id_grupo_seq', 5, true);


--
-- TOC entry 3628 (class 0 OID 0)
-- Dependencies: 236
-- Name: guardies_id_guardia_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.guardies_id_guardia_seq', 8540, true);


--
-- TOC entry 3629 (class 0 OID 0)
-- Dependencies: 238
-- Name: sessions_horari_id_sessio_seq; Type: SEQUENCE SET; Schema: public; Owner: magiuser
--

SELECT pg_catalog.setval('public.sessions_horari_id_sessio_seq', 95, true);


--
-- TOC entry 3376 (class 2606 OID 17559)
-- Name: aula aula_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.aula
    ADD CONSTRAINT aula_pkey PRIMARY KEY (id_aula);


--
-- TOC entry 3378 (class 2606 OID 17561)
-- Name: ausencies ausencies_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies
    ADD CONSTRAINT ausencies_pkey PRIMARY KEY (id_ausencia);


--
-- TOC entry 3382 (class 2606 OID 17563)
-- Name: ausencies_sessio ausencies_sessio_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT ausencies_sessio_pkey PRIMARY KEY (id_ausencia, id_sessio);


--
-- TOC entry 3384 (class 2606 OID 17565)
-- Name: ausencies_sessio ausencies_sessio_uniq; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT ausencies_sessio_uniq UNIQUE (id_ausencia, id_sessio);


--
-- TOC entry 3387 (class 2606 OID 17567)
-- Name: docent docent_document_key; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent
    ADD CONSTRAINT docent_document_key UNIQUE (document);


--
-- TOC entry 3389 (class 2606 OID 17569)
-- Name: docent docent_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent
    ADD CONSTRAINT docent_pkey PRIMARY KEY (id_docent);


--
-- TOC entry 3393 (class 2606 OID 17571)
-- Name: docent_sessio docent_sessio_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio
    ADD CONSTRAINT docent_sessio_pkey PRIMARY KEY (id_asignacion);


--
-- TOC entry 3391 (class 2606 OID 17573)
-- Name: docent docent_username_key; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent
    ADD CONSTRAINT docent_username_key UNIQUE (username);


--
-- TOC entry 3396 (class 2606 OID 17575)
-- Name: fichaje fichaje_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.fichaje
    ADD CONSTRAINT fichaje_pkey PRIMARY KEY (id);


--
-- TOC entry 3399 (class 2606 OID 17577)
-- Name: fichajes fichajes_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.fichajes
    ADD CONSTRAINT fichajes_pkey PRIMARY KEY (id);


--
-- TOC entry 3401 (class 2606 OID 17579)
-- Name: grupo grupo_nombre_key; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.grupo
    ADD CONSTRAINT grupo_nombre_key UNIQUE (nombre);


--
-- TOC entry 3403 (class 2606 OID 17581)
-- Name: grupo grupo_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.grupo
    ADD CONSTRAINT grupo_pkey PRIMARY KEY (id_grupo);


--
-- TOC entry 3405 (class 2606 OID 17583)
-- Name: guardies guardies_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT guardies_pkey PRIMARY KEY (id_guardia);


--
-- TOC entry 3407 (class 2606 OID 17585)
-- Name: guardies guardies_uniq; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT guardies_uniq UNIQUE (docent_absent, id_sessio, fecha_guardia);


--
-- TOC entry 3412 (class 2606 OID 17587)
-- Name: sessions_horari sessions_horari_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari
    ADD CONSTRAINT sessions_horari_pkey PRIMARY KEY (id_sessio);


--
-- TOC entry 3414 (class 2606 OID 17589)
-- Name: sessions_horari sessions_horari_uniq; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari
    ADD CONSTRAINT sessions_horari_uniq UNIQUE (plantilla, dia_setmana, sessio_ordre);


--
-- TOC entry 3417 (class 2606 OID 17591)
-- Name: sessions_horari sessions_horari_uniq_pl_dia_ordre; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari
    ADD CONSTRAINT sessions_horari_uniq_pl_dia_ordre UNIQUE (plantilla, dia_setmana, sessio_ordre);


--
-- TOC entry 3409 (class 2606 OID 17593)
-- Name: guardies uq_guardies_sessio_fecha; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT uq_guardies_sessio_fecha UNIQUE (id_sessio, fecha_guardia);


--
-- TOC entry 3419 (class 2606 OID 17595)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (dni);


--
-- TOC entry 3379 (class 1259 OID 17596)
-- Name: ausencies_unq; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE UNIQUE INDEX ausencies_unq ON public.ausencies USING btree (id_docent, fecha_ausencia);


--
-- TOC entry 3385 (class 1259 OID 17597)
-- Name: docent_document_idx; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE UNIQUE INDEX docent_document_idx ON public.docent USING btree (document);


--
-- TOC entry 3394 (class 1259 OID 17598)
-- Name: docent_sessio_uidx; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE UNIQUE INDEX docent_sessio_uidx ON public.docent_sessio USING btree (id_docent, id_sessio);


--
-- TOC entry 3380 (class 1259 OID 17599)
-- Name: idx_ausencies_fecha_docent; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE INDEX idx_ausencies_fecha_docent ON public.ausencies USING btree (fecha_ausencia, id_docent);


--
-- TOC entry 3397 (class 1259 OID 17600)
-- Name: idx_fichaje_dni_fecha; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE INDEX idx_fichaje_dni_fecha ON public.fichaje USING btree (dni, fecha_hora);


--
-- TOC entry 3410 (class 1259 OID 17601)
-- Name: idx_sessions_horari_horas; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE INDEX idx_sessions_horari_horas ON public.sessions_horari USING btree (hora_desde, hora_fins);


--
-- TOC entry 3415 (class 1259 OID 17602)
-- Name: sessions_horari_uniq_idx; Type: INDEX; Schema: public; Owner: magiuser
--

CREATE UNIQUE INDEX sessions_horari_uniq_idx ON public.sessions_horari USING btree (plantilla, dia_setmana, sessio_ordre);


--
-- TOC entry 3434 (class 2620 OID 17603)
-- Name: fichaje tr_revoca_ausencia; Type: TRIGGER; Schema: public; Owner: magiuser
--

CREATE TRIGGER tr_revoca_ausencia AFTER INSERT ON public.fichaje FOR EACH ROW WHEN (((new.tipo)::text = 'entrada'::text)) EXECUTE FUNCTION public.revoca_ausencia();


--
-- TOC entry 3435 (class 2620 OID 17604)
-- Name: guardies trg_check_tiempo_guardia; Type: TRIGGER; Schema: public; Owner: magiuser
--

CREATE TRIGGER trg_check_tiempo_guardia BEFORE INSERT ON public.guardies FOR EACH ROW EXECUTE FUNCTION public.check_guardia_tiempo();


--
-- TOC entry 3433 (class 2620 OID 17605)
-- Name: docent trg_sync_docent_usuario; Type: TRIGGER; Schema: public; Owner: magiuser
--

CREATE TRIGGER trg_sync_docent_usuario BEFORE INSERT OR UPDATE ON public.docent FOR EACH ROW EXECUTE FUNCTION public.sync_docent_usuario();


--
-- TOC entry 3436 (class 2620 OID 17606)
-- Name: usuarios trg_usuarios_docent; Type: TRIGGER; Schema: public; Owner: magiuser
--

CREATE TRIGGER trg_usuarios_docent AFTER INSERT ON public.usuarios FOR EACH ROW EXECUTE FUNCTION public.usuarios_to_docent();


--
-- TOC entry 3428 (class 2606 OID 17607)
-- Name: guardies fk_absent; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT fk_absent FOREIGN KEY (docent_absent) REFERENCES public.docent(id_docent);


--
-- TOC entry 3429 (class 2606 OID 17612)
-- Name: guardies fk_assignat; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT fk_assignat FOREIGN KEY (docent_assignat) REFERENCES public.docent(id_docent);


--
-- TOC entry 3421 (class 2606 OID 17617)
-- Name: ausencies_sessio fk_ausencia; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT fk_ausencia FOREIGN KEY (id_ausencia) REFERENCES public.ausencies(id_ausencia);


--
-- TOC entry 3424 (class 2606 OID 17622)
-- Name: docent_sessio fk_docent; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio
    ADD CONSTRAINT fk_docent FOREIGN KEY (id_docent) REFERENCES public.docent(id_docent);


--
-- TOC entry 3420 (class 2606 OID 17627)
-- Name: ausencies fk_docent_ausencia; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies
    ADD CONSTRAINT fk_docent_ausencia FOREIGN KEY (id_docent) REFERENCES public.docent(id_docent);


--
-- TOC entry 3423 (class 2606 OID 17632)
-- Name: docent fk_docent_usuario; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent
    ADD CONSTRAINT fk_docent_usuario FOREIGN KEY (document) REFERENCES public.usuarios(dni) ON UPDATE CASCADE ON DELETE RESTRICT DEFERRABLE INITIALLY DEFERRED;


--
-- TOC entry 3426 (class 2606 OID 17637)
-- Name: fichaje fk_fichaje_usuario; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.fichaje
    ADD CONSTRAINT fk_fichaje_usuario FOREIGN KEY (dni) REFERENCES public.usuarios(dni);


--
-- TOC entry 3430 (class 2606 OID 17642)
-- Name: guardies fk_guardia_sessio; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.guardies
    ADD CONSTRAINT fk_guardia_sessio FOREIGN KEY (id_sessio) REFERENCES public.sessions_horari(id_sessio);


--
-- TOC entry 3425 (class 2606 OID 17647)
-- Name: docent_sessio fk_sessio; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.docent_sessio
    ADD CONSTRAINT fk_sessio FOREIGN KEY (id_sessio) REFERENCES public.sessions_horari(id_sessio);


--
-- TOC entry 3422 (class 2606 OID 17652)
-- Name: ausencies_sessio fk_sessio_ausencia; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.ausencies_sessio
    ADD CONSTRAINT fk_sessio_ausencia FOREIGN KEY (id_sessio) REFERENCES public.sessions_horari(id_sessio);


--
-- TOC entry 3431 (class 2606 OID 17657)
-- Name: sessions_horari fk_sessions_horari_aula; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari
    ADD CONSTRAINT fk_sessions_horari_aula FOREIGN KEY (id_aula) REFERENCES public.aula(id_aula);


--
-- TOC entry 3432 (class 2606 OID 17662)
-- Name: sessions_horari fk_sessions_horari_grupo; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.sessions_horari
    ADD CONSTRAINT fk_sessions_horari_grupo FOREIGN KEY (id_grupo) REFERENCES public.grupo(id_grupo);


--
-- TOC entry 3427 (class 2606 OID 17667)
-- Name: fichajes fkslqvgdm6c5cpah6og36heklt3; Type: FK CONSTRAINT; Schema: public; Owner: magiuser
--

ALTER TABLE ONLY public.fichajes
    ADD CONSTRAINT fkslqvgdm6c5cpah6og36heklt3 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(dni);


--
-- TOC entry 3611 (class 0 OID 0)
-- Dependencies: 7
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: magiuser
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;


-- Completed on 2025-06-04 19:35:11 CEST

--
-- PostgreSQL database dump complete
--

