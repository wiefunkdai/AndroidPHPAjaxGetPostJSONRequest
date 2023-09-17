<?php
/**
 * #AndroidAjaxGetSendRequest
 * Implementation of JSON Function API based on
 * Request POST, GET, PUT, DELETE methods on Android
 *
 * @author  Stephanus Bagus Saputra
 *          ( 戴 Dai 偉 Wie 峯 Funk )
 * @link    http://www.stephanusdai.web.id
 * @copyright Copyright (c) ID 2023 Stephanus Bagus Saputra
 */
$input=file_get_contents("php://input");
$data = array('status'=>'OK');
$post = (!empty($input) ? $input : array());
if (isset($_POST) && !empty($_POST)) {
	$post = $_POST;
} else if (isset($_GET) && !empty($_GET)) {
	$post = $_GET;
} else if (isset($_PUT) && !empty($_PUT)) {
	$post = $_PUT;
}
if (isset($_FILES) && isset($_FILES['avatar']) && !empty($_FILES)) {
	$uploadDir = dirname(__FILE__);
	$fileName = $_FILES['avatar']['name'];
	$fileUpload = $_FILES['avatar']['tmp_name'];
	$pathUpload = $uploadDir . DIRECTORY_SEPARATOR . $fileName;
	if (move_uploaded_file($fileUpload, $pathUpload)) {
		$post['photo'] = 'http://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/' . $fileName;
	}
}

$data['content'] = $post;
header('Content-Type: application/json; charset=utf-8');
echo json_encode($data);