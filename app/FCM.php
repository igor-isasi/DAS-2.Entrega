<?php
    $cabecera=array(
        'Authorization: key=AAAAUPkVlmg:APA91bF1PTJj0ieX8_V5RUUax8YXoYSm95AJVXSzmC5nqIKxnmgExQ931kSnAlOhZi5MfgNAYiONcSqLLyCY6N2JJAm3pzQ4TJCPQxbFIzJJ5OPPhOvkDvXFR9QJal40p_hTruawXc6S',
        'Content-Type: application/json'
    );
    # Recoger los parámetros de la aplicación
    $parametros=json_decode(file_get_contents('php://input'), true);
    $token=$parametros["token"];
    $hora = date('H:i');
    $msg=array(
        'to' => $token,
        'data' => array(
            "hora" => $hora,
        )
    );
    $msgJSON=json_encode($msg);

    $ch = curl_init(); #inicializar el handler de curl
    #indicar el destino de la petición, el servicio FCM de google
    curl_setopt( $ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
    #indicar que la conexión es de tipo POST
    curl_setopt( $ch, CURLOPT_POST, true );
    #agregar las cabeceras
    curl_setopt( $ch, CURLOPT_HTTPHEADER, $cabecera);
    #Indicar que se desea recibir la respuesta a la conexión en forma de string
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
    #agregar los datos de la petición en formato JSON
    curl_setopt( $ch, CURLOPT_POSTFIELDS, $msgJSON );
    #ejecutar la llamada
    $resultado= curl_exec( $ch );
    #cerrar el handler de curl
    curl_close( $ch );

    if (curl_errno($ch)) {
        print curl_error($ch);
    }
    echo $resultado;
?>