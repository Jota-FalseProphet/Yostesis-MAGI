Mi Proyecto pesa demasiado para Aules. Está subido en formato completo a GitHub en un repositorio público (https://github.com/Jota-FalseProphet/Yostesis-MAGI), pero si prefieren algo menos lioso o les da pereza, en drive tengo todo igualmente: https://drive.google.com/drive/folders/1IHEHfX2o1vlzngJcIzVpimVxJruxdn50?usp=sharing

Igualmente recomiendo encarecidamente revisar el código desde GitHub, porque Google me presenta problemas a la hora de subir archivos .apk, .jar, etc...

No es necesario descargar ni el .sql, ni el PostgreSQL, ni el jar ni nada. La APK tiene mi licencia y se conectará en cuanto se instale en sus móviles directamente al backend hecho en SpringBoot.

El usuario Administrador tiene las credenciales:
--- Usuario: admin
--- Contraseña: admin

El usuario que comunmente utlizo (porque no me acuerdo de las credenciales del resto ya que están hasheadas):
--- Usuario: 1234
--- Contraseña: 1234

Este es con el que yo he cubierto ya las guardias para hacer pruebas y tal, por lo que en "Histórico de guardias" tiene un montón de guardais cubiertas por sí mismo. El resto simplemente tiene un montón de ausencias a sesiones a lo loco. Si quisieran probar otro usuario tendrían que cambiar la contraseña directamente a la base de datos, por lo que el único que puede hacerlo soy yo, ya que están hasheadas.

Las guardias se tienen que hacer dentro de las horas que pone ahí, por ejemplo:
Si son las 22:50 horas y la última ausencia fue a las 19:40, si se intenta cubrir, saltará error para evitar falsos cubiertos.

Si encesitan más usuarios les doy un par más:
012951758W changeme
33580118 changeme

