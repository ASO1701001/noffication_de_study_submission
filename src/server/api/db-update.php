<?php
require_once '../libs/RequestManager.php';

if (!isset($_GET['last_update_date'])) {
    $json = ['status' => 'E00', 'msg' => 'REQUIRED_PARAM'];
} else {
    $data = RequestManager::dbUpdate($_GET['last_update_date']);
    $json = ['status' => 'S00', 'data' => $data];
}

header('Access-Control-Allow-Origin: *');
header("Content-Type: application/json; charset=utf-8");
echo json_encode($json, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);