package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws IOException {
        HashMap<Integer, Integer> cash = new HashMap<>(); //Купюры, которые есть в банкомате (номинал : количество)
        //Заполняем словарь номиналом, с которым мы работаем
        for (int i : new int[]{1, 3, 5, 10, 25, 50, 100, 500, 1000, 5000}) {
            cash.put(i, 0);
        }
        boolean quit = false;
        while (!quit) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String s = reader.readLine();
            System.out.println(">"+s);
            String[] sList = s.split(" ");
            switch(sList[0]) {
                case "put":
                    if (!cash.containsKey(Integer.parseInt(sList[1]))) {
                        System.out.println("Банкомат не работает с номиналом " + sList[1] + " руб"); break;
                    }
                    if (Integer.parseInt(sList[2]) < 0) {
                        System.out.println("Некорректно задано кол-во купюр"); break;
                    }
                    putCash(cash, Integer.parseInt(sList[1]), Integer.parseInt(sList[2])); break;
                case "get":
                    if (!cash.containsKey(Integer.parseInt(sList[1]))) {
                        System.out.println("Банкомат не работает с номиналом " + sList[1] + " руб"); break;
                    }
                    optimize_get(cash, Integer.parseInt(sList[1]));
                    break;
                case "dump":
                    dump(cash);
                    break;
                case "state":
                    System.out.println(fullCash(cash)); break;
                case "quit":
                    quit = true; break;
                default:
                    break;
            }
        }
    }

    //Подсчитываем сколько всего денег в банкомате
    public static int fullCash(HashMap<Integer, Integer> cash) {
        int result = 0;
        for (HashMap.Entry<Integer, Integer> pair : cash.entrySet()) {
            result += pair.getValue() * pair.getKey();
        }
        return result;
    }

    //Кладёт в банкомат деньги
    public static void putCash(HashMap<Integer, Integer> cash, int nominal, int count) {
        cash.put(nominal, cash.get(nominal) + count);
        System.out.println("всего " + fullCash(cash));
    }

    //Выводит какие купюры и сколько есть в банкомате
    public static void dump(HashMap<Integer, Integer> cash) {
        for (int nominal : new int[]{5000, 1000, 500, 100, 50, 25, 10, 5, 3, 1}) {
            System.out.println(nominal + " " + cash.get(nominal));
        }
    }

    public static void print_get(HashMap<Integer, Integer> result, int amount) {
        String s = "";
        for (HashMap.Entry<Integer, Integer> res_pair : result.entrySet()) {
            if (res_pair.getValue() > 0) {
                s += res_pair.getKey() + "=" + res_pair.getValue() + ",";
            }

        }
        s += " всего " + fullCash(result);
        System.out.println(s);
        if (amount - fullCash(result) > 0) {
            System.out.println("без " + (amount - fullCash(result)));
        }
    }

    //Оптимизирует способ выдачи необходимой суммы и выводит её на экран
    public static void optimize_get(HashMap<Integer, Integer> cash, int amount) {
        if (amount <= 0) {
            System.out.println("Некорректно задана сумма");
            return ;
        }
        HashMap<Integer, Integer> res = new HashMap<>(get(new HashMap<Integer, Integer>(cash), amount, false, 0, false)); //Здесь хранится необходимая (возможно не вся) сумма маленькими купюрами
        //Удаляем из cash деньги, которые положили в result
        for (HashMap.Entry<Integer, Integer> pair : res.entrySet()) {
            cash.put(pair.getKey(), cash.get(pair.getKey()) - pair.getValue());
        }
        //Проходимся по купюрам побольше, чтобы заменить маленькие в result на более большие и не выдавать мелочью
        for (int nominal: new int[]{3, 5, 10, 25, 50, 100, 500, 1000, 5000}) {
            while (cash.get(nominal) > 0) {
                if (nominal <= amount) { //Купюра должна не больше необходимой суммы
                    HashMap<Integer, Integer> replacement = new HashMap<>(get(new HashMap<Integer, Integer>(res), nominal, true, amount - fullCash(res), false)); //Здесь мы заменяем маленькие купюры на одну nominal
                    if (!replacement.isEmpty()) { //Если replacement пуст, то nominal заменить целиком невозможно
                        print_get(res, amount);
                        print_get(replacement, nominal);

                        //Вернём в cash маленькие купюры, а из res удалим их
                        for (HashMap.Entry<Integer, Integer> pair : replacement.entrySet()) {
                            cash.put(pair.getKey(), cash.get(pair.getKey()) + pair.getValue());
                            res.put(pair.getKey(), res.get(pair.getKey()) - pair.getValue());
                        }
                        res.put(nominal, res.get(nominal) + 1); //Добавим одну купюру nominal в res
                        cash.put(nominal, cash.get(nominal) - 1); //Удалим одну купюру nominal из cash
                    } else break;
                } else break;
            }
        }

        //Пытаемся выдать сумму меньшим количеством купюр
        for (int nominal: new int[]{5000, 1000, 500, 100, 50, 25, 10, 5, 3, 1}) {
            while (cash.get(nominal) > 0) {
                if (nominal <= amount) { //Купюра должна не больше необходимой суммы
                    //Когда мы тут меняем rec на true, но замена происходит, но мы не додаём денег
                    HashMap<Integer, Integer> replacement = new HashMap<>(get(new HashMap<Integer, Integer>(res), nominal, true, 0, true)); //Здесь мы заменяем маленькие купюры на одну nominal
                    //Условие ниже не срабатывает почему-то
                    if (!replacement.isEmpty()) { //Если replacement пуст, то nominal заменить целиком невозможно
                        //Вернём в cash маленькие купюры, а из res удалим их
                        for (HashMap.Entry<Integer, Integer> pair : replacement.entrySet()) {
                            cash.put(pair.getKey(), cash.get(pair.getKey()) + pair.getValue());
                            res.put(pair.getKey(), res.get(pair.getKey()) - pair.getValue());
                        }
                        res.put(nominal, res.get(nominal) + 1); //Добавим одну купюру nominal в res
                        cash.put(nominal, cash.get(nominal) - 1); //Удалим одну купюру nominal из cash
                    } else break;
                } else break;
            }
        }

        print_get(res, amount);
    }

    //Эта фукнция возвращает словарь с маленькими купюрами. Из cash вычитает то, что вернуло.
    //(more - это сумма, которую мы должны додать)
    public static HashMap<Integer, Integer> get(HashMap<Integer, Integer> cash, int amount, boolean rec, int more, boolean reverse_nominals) {
        amount += more;
        HashMap<Integer, Integer> result = new HashMap<>();
        result.put(1, 0);
        result.put(3, 0);
        result.put(5, 0);
        result.put(10, 0);
        result.put(25, 0);
        result.put(50, 0);
        result.put(100, 0);
        result.put(500, 0);
        result.put(1000, 0);
        result.put(5000, 0);
        int sum = 0; //Сколько мы уже насчитали денег
        int[] nominals = {1, 3, 5, 10, 25, 50, 100, 500, 1000, 5000}; //Здесь мы храним список с номиналами
        //Переворачиваем nominals
        if (reverse_nominals) {
            for(int i = 0; i < nominals.length / 2; i++) {
                int temp = nominals[i];
                nominals[i] = nominals[nominals.length - i - 1];
                nominals[nominals.length - i - 1] = temp;
            }
        }
        for (int nominal: nominals) {
            for (int i = 0; i < cash.get(nominal); i++) { //Проходимся по количеству купюр конкретного номинала
                if (sum + nominal <= amount && cash.get(nominal) > 0) {
                    sum += nominal;
                    result.put(nominal, result.get(nominal) + 1);
                } else {
                    break;
                }
            }
        }
        for (Map.Entry<Integer, Integer> pair_res : result.entrySet()) {
            cash.put(pair_res.getKey(), cash.get(pair_res.getKey()) - pair_res.getValue());
        }

        if (fullCash(result) != amount) {
            int need = amount - fullCash(result);
            HashMap<Integer, Integer> replacement = new HashMap<>(); //Массив из маленьких купюр, которые хотим заменить одной большой
            replacement.put(1, 0);
            replacement.put(3, 0);
            replacement.put(5, 0);
            replacement.put(10, 0);
            replacement.put(25, 0);
            replacement.put(50, 0);
            replacement.put(100, 0);
            replacement.put(500, 0);
            replacement.put(1000, 0);
            replacement.put(5000, 0);

            //Проходимся по номиналам больших купюр, на которые хотим поменять
            for (int nominal_cash: nominals) {
                if (nominal_cash == 0) continue;
                if (cash.get(nominal_cash) > 0) {
                    boolean used_cash_nominal = true; //Произошла замена или нет
                    while (used_cash_nominal) { //Если произошла замена с этой купюрой, то можно повторить
                        used_cash_nominal = false;

                        //Проходимся по номиналам маленьких купюр, которые хотим заменить
                        for (int nominal_result: nominals) {
                            if (nominal_result == 5000) continue;
                            while (result.get(nominal_result) > 0) {

                                if (fullCash(result) == amount) break; //Если набрали всю сумму, то останавливаем цикл (иначе он всё из result спрячет в replacement)

                                replacement.put(nominal_result, replacement.get(nominal_result) + 1);
                                result.put(nominal_result, result.get(nominal_result) - 1);

                                //Если есть смысл менять накопленные купюры на 1 большую, то поменяем
                                if (nominal_cash - fullCash(replacement) - nominal_result <= need && nominal_cash - fullCash(replacement) - nominal_result >= 0) {
                                    replacement.put(nominal_result, replacement.get(nominal_result) + 1); //Добавим ещё одну маленткую купюру (т.к. её мы учитывали в проверку)
                                    result.put(nominal_result, result.get(nominal_result) - 1); //Её же удалим из словаря result

                                    //Все купюры из replacement возвращаем в cash
                                    for (Map.Entry<Integer, Integer> pair_replacement : replacement.entrySet()) {
                                        if (pair_replacement.getValue() > 0) {
                                            cash.put(pair_replacement.getKey(), cash.get(pair_replacement.getKey()) + pair_replacement.getValue());
                                            replacement.put(pair_replacement.getKey(), 0); //Обнуляем replacement
                                        }
                                    }

                                    //В result добавляем 1 купюру номиналом nominal_cash
                                    result.put(nominal_cash, result.get(nominal_cash) + 1); //Тут что-то не так
                                    need = amount - fullCash(result);

                                    //Из cash удаляем одню купюру номиналом nominal_cash
                                    cash.put(nominal_cash, cash.get(nominal_cash) - 1);

                                    used_cash_nominal = true;
                                    sum = fullCash(result);

                                    //Пытаемся дополнить словать result маленькими купюрами (ТОТ ЖЕ ЦИКЛ ЧТО И В СТРОКЕ 95 !!!, можно объединить в метод)
                                    for (int nominal: nominals) {
                                        for (int i = 0; i < cash.get(nominal); i++) {
                                            if (sum + nominal <= amount && cash.get(nominal) > 0) {
                                                sum += nominal;
                                                result.put(nominal, result.get(nominal) + 1);
                                                cash.put(nominal, cash.get(nominal) - 1);
                                            }
                                        }
                                    }
                                }
                            }
                            if (used_cash_nominal) break; //Для того, чтобы мы дальшше по массиву не шли, а начали с самого начала
                        }
                    }
                }
            }
            //Если нам не удалось в итоге поменять маленькие купюры на одну большую, то из replacement всё выкладываем обратно в result
            for (Map.Entry<Integer, Integer> pair : replacement.entrySet()) {
                result.put(pair.getKey(), result.get(pair.getKey()) + pair.getValue());
            }
        }

        if (rec) {
            //Надо проверить, если выдаст только одну купюру (номиналом в запрашиваемую сумму), то мы должны выдать пустой словарь
            if (result.get(amount-more) == 1) {
                return new HashMap<>();
            } else if (fullCash(result) >= (amount-more-more) && fullCash(result) <= (amount-more)) { //Вопрос поменять минут на плюс. ТУТ ПОДУМАТЬ (данный пример РАБОТАЕТ!!!но выдаёт не самыми крупными купюрами)
                return result;
            } else {
                return new HashMap<>();
            }
        }
        return result;
    }
}
