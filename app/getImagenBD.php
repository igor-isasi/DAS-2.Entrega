<?php
    $DB_SERVER="127.0.0.1"; #la dirección del servidor
	$DB_USER="Xiisasi021"; #el usuario para esa base de datos
	$DB_PASS="rJvH5EYjUM"; #la clave para ese usuario
	$DB_DATABASE="Xiisasi021_DAS2"; #la base de datos a la que hay que conectarse
	# Se establece la conexión:
	$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);
	#Comprobamos conexión
	if (mysqli_connect_errno()) {
		echo 'Error de conexion: ' . mysqli_connect_error();
		exit();
	}
	# Recoger los parámetros de la aplicación
	$parametros=json_decode(file_get_contents('php://input'), true);
	$usuario=$parametros["usuario"];
    $resultado = mysqli_query($con, "SELECT imagen FROM Imagenes WHERE user_fk='$usuario'");
     # Comprobar si se ha ejecutado correctamente
     if (!$resultado) {
       	echo 'Ha ocurrido algún error: ' . mysqli_error($con);
     } else {
        $array_resultado = mysqli_fetch_array($resultado);
        echo $array_resultado['imagen'];
     }
?>