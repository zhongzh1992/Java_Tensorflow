package org.demo.bert.predict;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.tensorflow.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class CallTensorflowModel {

    public static int seqLength = 128;
    public static final String vacab_file_path = "/Users/zhongzihao/PycharmProjects/bert-for-task/bert_task/bert_model/chinese_L-12_H-768_A-12/vocab.txt";

    /**
     * main.
     *
     * @param args args
     */
    public static void main(String[] args) throws Exception {
        String path = "/Users/zhongzihao/PycharmProjects/bert-for-task/bert_task/classifier_task/ckpt_model/inews/first.pb";
        try (Graph graph = new Graph()) {
            //导入图
            byte[] graphBytes = IOUtils.toByteArray(new FileInputStream(path));
            graph.importGraphDef(graphBytes);

            //根据图建立Session
            try (Session session = new Session(graph)) {
                List<String> idx = sentence_to_idx("烟机调到中间风");
                String strInputIds = idx.get(0);
                Tensor<Integer> inputIds = fromStringToTensor(strInputIds, seqLength);
                String strInputMask = idx.get(1);
                Tensor<Integer> inputMask = fromStringToTensor(strInputMask, seqLength);
                String strInputTypeIds = idx.get(2);
                Tensor<Integer> inputTypeIds = fromStringToTensor(strInputTypeIds, seqLength);

                Stopwatch stopwatch = Stopwatch.createStarted();
                Tensor out = session.runner().
                        feed("input_ids", inputIds).
                        feed("input_mask", inputMask).
                        feed("segment_ids", inputTypeIds)
                        .fetch("output/predictions")
                        .run().get(0);

                System.out.println(String.format("time cost %d", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
//                System.out.println(out);
                long[] arr = new long[1];//0 1
                out.copyTo(arr);
                for (int i = 0; i < 1; i++) {
                    System.out.println(arr[i]);
                }
            }
        }
    }

    private static Tensor<Integer> fromStringToTensor(String input, int length) {
        int[] arr = Splitter.on(',')
                .trimResults().omitEmptyStrings().splitToList(input).stream()
                .mapToInt(x -> Integer.valueOf(x))
                .toArray();
        Preconditions.checkArgument(length == arr.length);
        Tensor<Integer> tensor = Tensors.create(new int[][]{arr});
        return tensor;
    }

    public static List<String> sentence_to_idx(String text) throws IOException {
        String[] tokens = getTokens(text);
        String input_ids = convert_tokens_to_ids(tokens);
        String input_mask = getSequenceByChar("1", tokens.length);
        String segment_id = getSequenceByChar("0", tokens.length);
        List<String> list = new ArrayList<>();
        list.add(padding(input_ids, tokens.length, seqLength));
        list.add(padding(input_mask, tokens.length, seqLength));
        list.add(padding(segment_id, tokens.length, seqLength));
        return list;
    }

    public static String[] getTokens(String text) {
        int index = 0;
        String[] tokens = new String[text.length() + 2];
        tokens[0] = "[CLS]";
        index++;
        for (char item : text.toCharArray()) {
            tokens[index++] = String.valueOf(item);
        }
        tokens[text.length() + 1] = "[SEP]";
        return tokens;
    }

    public static String padding(String arr, int len, int maxLength) {
        String result = "";
        if (len < maxLength) {
            result = arr + "," + getSequenceByChar("0", maxLength - len);
        } else {
            String[] temp = arr.split(",");
            for (int i = 0; i < maxLength; i++) {
                result += temp[i] + ",";
            }
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static String getSequenceByChar(String item, int len) {
        int cnt = 0;
        String result = "";
        while (cnt < len) {
            result += item + ",";
            cnt++;
        }
        return result.substring(0, result.length() - 1);
    }

    public static String convert_tokens_to_ids(String[] tokens) throws IOException {
        Map<String, Integer> vacab = load_vacab();
        String result = "";
        for (String token : tokens) {
            result += vacab.get(token).toString() + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    public static Map load_vacab() throws IOException {
        Map<String, Integer> vacab = new HashMap<>();
        FileReader fileReader = new FileReader(vacab_file_path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        int index = 0;
        while ((line = bufferedReader.readLine()) != null) {
            vacab.put(line, index);
            index++;
        }
        bufferedReader.close();
        fileReader.close();
        return vacab;
    }
}
