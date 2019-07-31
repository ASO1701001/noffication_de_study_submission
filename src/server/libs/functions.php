<?php
function random(int $count) : string {
    $string = null;
    for ($i = 0; $i < $count; $i++) {
        try {
            $string .= random_int(0, 9);
        } catch (Exception $e) {
            $string .= 0;
        }
    }

    return $string;
}