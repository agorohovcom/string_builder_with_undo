public class Main {
    public static void main(String[] args) {
        StringBuilderWithUndo sb = new StringBuilderWithUndo();
        sb.append("Hello, I'm AGorohov's simple StringBuilder class with \"Undo\" function! =)");
        System.out.println(sb);
        sb.delete(5, 50);
        System.out.println(sb);
        sb.undo();
        System.out.println(sb);
    }
}