package com.example.quest;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class QuestService {
    public record Quest(String id, String chapter, String title, String question, String code,
                        List<String> options, String hint) {}
    public record Result(boolean correct, String explanation, int xp) {}
    private record Lesson(Quest quest, int answer, String explanation) {}
    private static Lesson lesson(String id, String chapter, String title, String question,
                                 String code, List<String> options, int answer, String hint, String explanation) {
        return new Lesson(new Quest(id, chapter, title, question, code, options, hint), answer, explanation);
    }
    private final List<Lesson> lessons = List.of(
        lesson("java-1", "Javaの森", "変数のたね", "経験値を整数で保存する型は？", "___ xp = 100;", List.of("String", "int", "boolean"), 1, "小数を含まない数値を扱います。", "int は整数型です。String は文字列、boolean は true / false を表します。"),
        lesson("java-2", "Javaの森", "分岐の道", "このコードで表示される文字は？", "int level = 5;\nif (level >= 5) {\n    System.out.println(\"勇者\");\n} else {\n    System.out.println(\"見習い\");\n}", List.of("見習い", "何も表示されない", "勇者"), 2, ">= は同じ値も含みます。", "5 >= 5 は true なので、if 側の「勇者」が表示されます。"),
        lesson("java-3", "Javaの森", "ループの橋", "ループが終わったときの coins は？", "int coins = 0;\nfor (int i = 0; i < 3; i++) {\n    coins += 10;\n}", List.of("30", "20", "40"), 0, "i が 0、1、2 のときに実行されます。", "3回の繰り返しで10ずつ増えるため、coins は30になります。"),
        lesson("java-4", "Javaの森", "文字列の合言葉", "文字列の内容を比較するには？", "String key = new String(\"spring\");\nboolean open = ___;", List.of("key == \"spring\"", "key.equals(\"spring\")", "key = \"spring\""), 1, "参照ではなく、内容を比べるメソッドです。", "equals は文字列の内容を比較します。== は参照の同一性を比較し、= は代入です。"),
        lesson("java-5", "オブジェクトの塔", "クラスの設計図", "Hero のインスタンスを作るコードは？", "class Hero {\n    String name;\n}", List.of("Hero hero = new Hero();", "Hero hero = Hero;", "new hero = class Hero();"), 0, "インスタンス生成には new を使います。", "クラスは設計図、インスタンスは実体です。new Hero() で生成し、Hero 型の変数に代入します。"),
        lesson("java-6", "オブジェクトの塔", "リストの宝箱", "names に格納できる値は？", "List<String> names = new ArrayList<>();\nnames.add(___);", List.of("42", "true", "\"勇者\""), 2, "<> の中は要素の型を指定します。", "List<String> は文字列のリストです。ジェネリクスにより、違う型の追加をコンパイル時に防ぎます。"),
        lesson("spring-1", "Springの街", "起動の魔法", "Spring Boot アプリの入口に付けるのは？", "___\npublic class QuestApplication {\n    public static void main(String[] args) {\n        SpringApplication.run(QuestApplication.class, args);\n    }\n}", List.of("@Entity", "@SpringBootApplication", "@GetMapping"), 1, "自動構成とコンポーネント探索も有効にします。", "@SpringBootApplication は構成クラス・自動構成・コンポーネントスキャンをまとめたアノテーションです。"),
        lesson("spring-2", "Springの街", "APIの受付", "戻り値をレスポンス本文として返すクラスに付けるのは？", "___\nclass HelloController {\n    @GetMapping(\"/hello\")\n    String hello() { return \"Hello!\"; }\n}", List.of("@Repository", "@Configuration", "@RestController"), 2, "REST API のコントローラーを表します。", "@RestController は @Controller と @ResponseBody を組み合わせ、戻り値を本文として返します。"),
        lesson("spring-3", "Springの街", "依存性の仲間", "このコンストラクターで service を渡すのは？", "@RestController\nclass QuestController {\n    private final QuestService service;\n    QuestController(QuestService service) {\n        this.service = service;\n    }\n}", List.of("Springコンテナ", "ブラウザー", "Javaコンパイラー"), 0, "登録された Bean を管理している仕組みです。", "Spring が QuestService の Bean を注入します。コンストラクターが1つの場合、@Autowired は省略できます。"),
        lesson("spring-4", "Springの街", "サービスの工房", "業務ロジックを担当するクラスの印は？", "___\nclass QuestService {\n    int reward() { return 100; }\n}", List.of("@GetMapping", "@Service", "@RequestBody"), 1, "サービス層の役割を示すアノテーションです。", "@Service はコンポーネントスキャンで Bean として登録されます。業務ロジックをコントローラーから分離できます。"),
        lesson("spring-5", "APIの城", "JSONの届け物", "JSON本文を引数に読み込むには？", "@PostMapping(\"/answers\")\nResult answer(___ Answer answer) {\n    return service.check(answer);\n}", List.of("@PathVariable", "@RequestParam", "@RequestBody"), 2, "URLではなくリクエストの本文です。", "@RequestBody はHTTPリクエスト本文をJavaオブジェクトへ変換します。@PathVariable はパス、@RequestParam はクエリなどを扱います。"),
        lesson("spring-6", "APIの城", "最後のルーティング", "GET /quests/7 の 7 を id に入れるには？", "@GetMapping(\"/quests/{id}\")\nQuest find(___ Long id) {\n    return service.find(id);\n}", List.of("@PathVariable", "@RequestBody", "@Service"), 0, "URLの {id} に対応させます。", "@PathVariable はURLパスの値を受け取ります。学んだ Controller → Service の流れは、このゲームのソースでも確認できます。")
    );

    public List<Quest> quests() { return lessons.stream().map(Lesson::quest).toList(); }

    public Result check(String id, Integer option) {
        Lesson lesson = lessons.stream().filter(l -> l.quest().id().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "問題が見つかりません"));
        if (option == null || option < 0 || option >= lesson.quest().options().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "選択肢を指定してください");
        }
        boolean correct = option == lesson.answer();
        return new Result(correct, correct ? lesson.explanation() : "もう一度考えてみましょう。ヒントも使えます。", correct ? 100 : 0);
    }
}
