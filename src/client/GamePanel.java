package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;

public class GamePanel extends JFrame implements ActionListener,Runnable{
    private int life=10;
    private char keyChar;
    private JLabel lbMoveChar =new JLabel();
    private JLabel lblife =new JLabel();

    private JTextArea answer=new JTextArea();
    private JLabel tips=new JLabel();

    private Socket s=null;
    private Timer timer=new Timer(300,  this);
    private Random rnd=new Random();
    private BufferedReader br=null;
    private PrintStream ps=null;

    private String word = null;
    private String Opt = null;
    private String chinese=null;
    private String tip=null;
    private int il;
    String strSave = null;
    String keyStr = null;

    private JButton start=new JButton();
    private Button sub=new Button();

    private boolean canRun = true;

    public GamePanel(){
        this.setLayout(null);
        this.setBackground(Color.darkGray);
        this.setSize(520,500);
        this.add(lblife);
        this.setVisible(true);


        lblife.setFont(new Font("黑体",Font.BOLD,20));
        lblife.setBackground(Color.yellow);
        lblife.setForeground(Color.PINK);
        lblife.setBounds(0,0,500,20);

        this.add(lbMoveChar);
        lbMoveChar.setFont(new Font("黑体",Font.BOLD,20));
        lbMoveChar.setForeground(Color.yellow);

        this.add(answer);
        answer.setFont(new Font("黑体",Font.BOLD,20));
        answer.setBounds(0,440,375,30);

        this.add(tips);
        tips.setFont(new Font("黑体",Font.BOLD,20));
        tips.setBounds(0,410,450,30);


        this.add(start);
        start.setBounds(375,440,150,30);
        start.setVisible(true);
        start.setLabel("subimit");
        start.setBackground(Color.yellow);
        start.setFont(new Font("黑体",Font.BOLD,20));
        start.addActionListener(this);

        try{
            s=new Socket("127.0.0.1",9999);
            System.out.println("连接成功");
            InputStream is=s.getInputStream();
            br=new BufferedReader(new InputStreamReader(is));
            OutputStream os=s.getOutputStream();
            ps=new PrintStream(os);
            new Thread(this).start();
        }catch (Exception ex){
            javax.swing.JOptionPane.showMessageDialog(this,"连接游戏异常退出！");
            System.exit(0);
        }
        this.init();

    }

    public synchronized void readLineFile(String filename,int il){
        try{
            FileInputStream fi=new FileInputStream(filename);
            InputStreamReader isr=new InputStreamReader(fi,"UTF-8");
            BufferedReader br=new BufferedReader(isr);
            while(br.readLine()!=null&&il>=0){
                il--;
                if(il<0){
                    String str1= br.readLine();

                    strSave =str1+"\r\n";

                    String[] strs1=str1.split("\\s+");
                    word=strs1[0];
                    chinese=strs1[1];

                    System.out.println("1单词："+word);
                    System.out.println("1单词源："+strs1[1]);

                }
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }
    public void init(){
        lblife.setText("当前生命值："+life);
        readLineFile("D:\\word.txt.txt",il);

        lbMoveChar.setText(chinese);
        lbMoveChar.setBounds(100,0,200,50);

        answer.setText("");

        tip=word.substring(0,2);
        tips.setText("tip:"+tip);
        timer.start();

    }

    public void writeFile(String filename,String str){
        try{
            FileOutputStream fos=new FileOutputStream(filename,true);
            byte[] b=str.getBytes();
            fos.write(b);
            fos.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void checkFail(){
        lblife.setText("当前生命值:"+life);

        if(life<=0){
            ps.println("WIN#");
            timer.stop();
            javax.swing.JOptionPane.showMessageDialog(this,"生命值耗尽，游戏结束");
            System.exit(0);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(lbMoveChar.getY()>=this.getHeight())
        {
            writeFile("D:\\未掌握单词.txt",strSave);
            life--;
            checkFail();
            ps.println("ASKRN");
        }
        if(e.getSource()==start){
            keyStr=answer.getText();
            //System.out.println(keyStr);
            try{
                if(keyStr.equalsIgnoreCase(word)){

                    writeFile("D:\\已掌握单词.txt",strSave);

                    life+=2;
                    ps.println("LIFE#-1");
                }
                else {
                    writeFile("D:\\未掌握单词.txt",strSave);
                    life-=2;
                    ps.println("LIFE#1");
                }
            }catch (Exception ex){
                canRun=false;
                javax.swing.JOptionPane.showMessageDialog(this,"对比游戏异常退出");
            }
        }
        lbMoveChar.setLocation(lbMoveChar.getX(), lbMoveChar.getY()+10);


    }
    @Override
    public void run() {
        try {
            while (canRun){
                String str= br.readLine();
                String[] strs=str.split("#");
                if(strs[0].equals("START")){
                    il=Integer.parseInt(strs[1]);
                }
                else if(strs[0].equals("LIFE")){
                    int score=Integer.parseInt(strs[1]);
                    life+=score;
                    checkFail();
                    if (strs[2].equals("START")){
                        il=Integer.parseInt(strs[3]);
                    }
                }
                else if(strs[0].equals("UWIN")){
                    timer.stop();
                    javax.swing.JOptionPane.showMessageDialog(this,"游戏结束你赢了");
                    System.exit(0);

                }
                init();
            }
        }catch (Exception ex){
            canRun=false;
            javax.swing.JOptionPane.showMessageDialog(this,"run游戏异常退出");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new GamePanel();
    }



}
