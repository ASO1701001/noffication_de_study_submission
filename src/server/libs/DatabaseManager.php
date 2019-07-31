<?php
class DatabaseManager {
    private $DB_SETTING = [
        'db_name' => '',
        'host' => '',
        'charset' => '',
        'user' => '',
        'password' => ''
    ];

    /**
     * @return PDO
     */
    function pdo() {
        $dsn = "mysql:dbname={$this->DB_SETTING['db_name']};host={$this->DB_SETTING['host']};port=3306;charset={$this->DB_SETTING['charset']}";
        try {
            $pdo = new PDO($dsn, $this->DB_SETTING['user'], $this->DB_SETTING['password']);
            $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
        } catch(Exception $e) {
            die($e->getMessage());
        }
        return $pdo;
    }

    /**
     * @param $pdo
     */
    function close(&$pdo) {
        $pdo = null;
    }

    /**
     * @param string $sql
     * @return array
     */
    function query(string $sql) {
        try {
            $pdo = $this->pdo();
            $stmt = $pdo->query($sql);
            $items = $stmt->fetchAll();
            $this->close($pdo);
        } catch (Exception $e) {
            die($e->getMessage());
        }
        return self::array_escape($items);
    }

    /**
     * @param string $sql
     * @param array $param
     * @return bool|PDOStatement
     */
    function execute(string $sql, array $param) {
        try {
            $pdo = $this->pdo();
            $stmt = $pdo->prepare($sql);
            $stmt->execute(self::array_escape($param));
            $this->close($pdo);
        } catch (Exception $e) {
            die($e->getMessage());
        }
        return $stmt;
    }

    /**
     * @param string $sql
     * @param array $param
     * @param string|null $id_column_name
     * @return string
     */
    function insert(string $sql, array $param, string $id_column_name = null) {
        try {
            $pdo = $this->pdo();
            $stmt = $pdo->prepare($sql);
            $stmt->execute(self::array_escape($param));
            $insert_id = $pdo->lastInsertId($id_column_name);
            $this->close($pdo);
        } catch (Exception $e) {
            die($e->getMessage());
        }
        return $insert_id;
    }

    /**
     * @param string $sql
     * @param array $param
     * @return array
     */
    function fetch(string $sql, array $param) {
        try {
            $stmt = $this->execute($sql, self::array_escape($param));
            $data = $stmt->fetch();
        } catch (Exception $e) {
            die($e->getMessage());
        }
        return self::array_escape($data);
    }

    /**
     * @param string $sql
     * @param array $param
     * @return int
     */
    function fetchColumn(string $sql, array $param) {
        try {
            $stmt = $this->execute($sql, self::array_escape($param));
            $data = $stmt->fetchColumn();
        } catch (Exception $e) {
            die($e->getMessage());
        }
        return self::escape($data);
    }

    /**
     * @param string $sql
     * @param array $param
     * @return array
     */
    function fetchAll(string $sql, array $param) {
        try {
            $stmt = $this->execute($sql, self::array_escape($param));
            $data = $stmt->fetchAll();
        } catch (Exception $e) {
            die($e->getMessage());
        }
        return self::array_escape($data);
    }

    /**
     * @param array $array
     * @return array
     */
    private static function array_escape($array) {
        if (is_null($array) || empty($array)) {
            return $array;
        }
        foreach ($array as $key => $value) {
            if (is_array($value)) {
                $array[$key] = self::array_escape($value);
            } else {
                $array[$key] = self::escape($value);
            }
        }
        return $array;
    }

    /**
     * @param $value
     * @return string
     */
    private static function escape($value) {
        return htmlspecialchars($value, ENT_QUOTES, 'UTF-8');
    }
}