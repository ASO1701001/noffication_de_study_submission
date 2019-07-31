<?php
require_once '../libs/RequestManager.php';

if (!isset($_POST['token'])) {
    $json = ['status' => 'E00', 'msg' => 'REQUIRED_PARAM'];
} else {
    $data = RequestManager::addUser($_POST['token']);
    $json = ['status' => 'S00', 'data' => ['user_id' => $data]];
}

header('Access-Control-Allow-Origin: *');
header("Content-Type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);