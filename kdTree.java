package kdTree;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
// the type of traning data: (1.2,4.5) 0
//search the nearest k point giving a input point in the kd tree
class Point{
	public static final int N=2;
	public double []point;
	public int Decision;
	Point()
	{
		point=new double[N];
	}
}
class Node{
	public Node lchild;
	public Node rchild;
	public Node father;
	Node()
	{
		flag=false;
	}
	public int location;
	public int dimen;
	public boolean flag;
}
public class kdTree {
	public static Node root;
	public final static int K=3;     // k个最近
	public final int NumStrategic=3;
	public static Node[] KNN;     // 存储最近K个节点的点在Value数组中的位置
	public static double[] KNNDistance;   // 存储最近K个节点到输入点的距离
	public static int KNum=0;   // 已存储最近点个数
	public static int Max=-1;     //　距离最大在ＫＮＮ数组中的位置
	public static double MaxDis=-1.0;   // 已存储中距离的最大值
	public static Point inPoint;     // 输入点
	public static Point[] Value;     //  训练点的存储数组
	  //HashMap<Point,Interger> P=new HashMap<Point,Interger>();
	public static HashSet<Point> set=new HashSet<Point>();
	public void QuickSort(int key,int i, int j){
		/**
		 * 快排
		 */
		if(i>=j)
			return;
		int p=0;
		int m=i;
		int n=j;
		Point swap;
		while(i<j)
		{
			while(i<j&&Value[i].point[key]<=Value[j].point[key])
			{
				--j;
			}
			if(i<j)
			{
			    swap=Value[i];
			    Value[i]=Value[j];
			    Value[j]=swap;
			    ++i;
			}
			while(i<j&&Value[i].point[key]<=Value[j].point[key])
			{
				++i;
			}
			if(i<j)
			{
			    swap=Value[i];
			    Value[i]=Value[j];
			    Value[j]=swap;			    
			    --j;
			}
			p=i;
		}
		QuickSort(key,m,p-1);
		QuickSort(key,p+1,n);
	     	
	}
	public void readPoint(Point a,String line){
		//读取点
		line=line.replace("(", "");
		line=line.replace(")", "");
		String []token=line.split(",");
		if(token.length!=Point.N)
		{
			return ;
		}
	//	Point a=new Point();
		for(int i=0;i<Point.N;i++)
		{
			a.point[i]=Double.parseDouble(token[i]);
		}
	}
	public void ReadDocument(String args0) throws IOException{
		BufferedReader is =new BufferedReader(new InputStreamReader(new FileInputStream(args0),"UTF-8"));
		String line;
		while((line=is.readLine())!=null)
		{
			line=line.trim();
			if(line.equals(""))
			{
				continue;
			}
			String[] str=line.split(" ");
			line=str[0];
			Point a=new Point();
			readPoint(a,line);
			a.Decision=Integer.parseInt(str[1]);
			set.add(a);
		}
		is.close();		
		Value=new Point[set.size()];
		int i=0;
		for(Point a:set)
		{
			Value[i]=a;
			i++;
		}
		//QuickSort
	}
	public Double getVAR(int key,int x,int y){
		// get the variance
		// D=E(X^2)-(EX)^2
		Double EX2=0.0;
		Double EX=0.0;
		for(int i=x;i<=y;i++)
		{
			EX2+=Math.pow(Value[i].point[key],2);
			EX+=Value[i].point[key];
		}
		EX2/=(y-x+1);
		EX/=(y-x+1);
		return EX2-Math.pow(EX, 2);
	}
	public int getKey(int x,int y){
		/**
		 * 得到当前划分的维度
		 */
		Double max=getVAR(0,x,y);
		int key=0;
		Double pp;
		for(int i=1;i<Point.N;i++)
			if(max<=(pp=getVAR(i,x,y)))
			{
				max=pp;
				key=i;
			}
		return key;
	}
	public void GenerateKdTree(int x,int y,Node current){
	//构造平衡kd树

		if(x>y)
		{
			if(current.father.lchild==current)
				current.father.lchild=null;
			else
				current.father.rchild=null;
			
			return;
		}
		int key=getKey(x,y);
		QuickSort(key,x,y);
		int p=(x+y)/2;		
		current.dimen=key;
		current.location=p;
		Node leftchild=new Node();
		current.lchild=leftchild;
		leftchild.father=current;
		Node rightchild=new Node();
		current.rchild=rightchild;
		rightchild.father=current;
		GenerateKdTree(x,p-1,leftchild);
		GenerateKdTree(p+1,y,rightchild);		
	}
	public Node getleafNode(Point a){
		Node current=root;
		while(current.lchild!=null && current.rchild!=null)
		{
			if(Value[current.location].point[current.dimen]>a.point[current.dimen])
				current=current.lchild;
			else
				current=current.rchild;
		}
		if(current.lchild!=null)
	   	   return current.lchild;
		else if(current.rchild!=null)
     	   return current.rchild;
		else
			return current;
	}
	public void Out(String args1,String args2) throws IOException, IOException{
		/**
		 * output decision
		 */
		BufferedReader is=new BufferedReader(new InputStreamReader(new FileInputStream(args1),"UTF-8"));
		OutputStreamWriter os=new OutputStreamWriter(new FileOutputStream(args2),"UTF-8");
		String line;
	//	Point a=new Point();
		while((line=is.readLine())!=null)
		{
			if(line.equals(""))
			{
				continue;
			}
			String str=line;
			inPoint=new Point();
			readPoint(inPoint,line);
			KNNDistance=new double[K];
			for(int i=0;i<K;i++)
				KNNDistance[i]=0.0;
			Node a=getleafNode(inPoint);
			KNN=new Node[K];
			KNN[KNum]=a;
			MaxDis=getDistance(a);
			KNNDistance[KNum]=MaxDis;
			Max=KNum;
			a.flag=true;
			++KNum;
			SearchKNN(a.father);
			KNNDecision();
			System.out.println(inPoint.Decision+"\t");
			for(int i=0;i<KNum;i++)
				System.out.print(Value[KNN[i].location].point[0]+","+Value[KNN[i].location].point[1]+" ");
			System.out.println();
			KNum=0;
			os.write(str+"\t"+inPoint.Decision+"\n");
		}
		is.close();
		os.close();
	}
	public void KNNDecision(){
		/**
		 * 举手表决
		 */
		int[] strategic=new int[NumStrategic];
		for(int i=0;i<KNum;i++)
			++strategic[Value[KNN[i].location].Decision];
		inPoint.Decision=0;
		for(int i=1;i<NumStrategic;i++)
			if(strategic[i]>strategic[inPoint.Decision])
			{
				inPoint.Decision=i;
			}
	}
	public double getDistance(Node a){
		/**
		 * 得到输入点与某一节点存储点的距离
		 */
		double distance=0.0;
		for(int i=0;i<Point.N;i++)
			distance+=Math.pow(Value[a.location].point[i]-inPoint.point[i], 2);
		distance=Math.sqrt(distance);
		return distance;
	}
	public int getMax(){
		/**
		 * 得到已找到的KNum个点的距离输入点最远的点
		 */
		int Max=0;
		for(int i=1;i<KNum;i++)
			if(KNNDistance[i]>KNNDistance[Max])
			{
				Max=i;
			}
		return Max;
			
	}
	public void DeclineTraverse(Node current){	
		if(current==null)
			return;
		if(current.flag==true)
		{
			current.flag=false;
			return;
		}
		Double Dis;
		current.flag=true;
		if(KNum<K){
			KNN[KNum]=current;
			Dis=getDistance(current);
			KNNDistance[KNum]=Dis;
			if(MaxDis<Dis)
			{
				MaxDis=Dis;
				Max=KNum;
			}
			++KNum;
		} else 	if ( (Dis=getDistance(current))<MaxDis ){
			
				    KNN[Max]=current;
			    	KNNDistance[Max]=Dis;
			        Max=getMax();
			        MaxDis=KNNDistance[Max];
			}
			
		   DeclineTraverse(current.lchild);
		   DeclineTraverse(current.rchild);
		
	}
	public void SearchKNN(Node current){
		if(current==null)
			return;
		double Dis=getDistance(current);
		double DimenDis=Math.abs(Value[current.location].point[current.dimen]-inPoint.point[current.dimen]);
		current.flag=true;
		if(KNum<K){
			KNN[KNum]=current;
			KNNDistance[KNum]=Dis;
			if(MaxDis<Dis)
			{
				Max=KNum;
				MaxDis=Dis;
			}
			++KNum;
		} else {
			if(MaxDis>DimenDis && Dis<MaxDis)
			{
	//			KNN[Max].flag
				KNN[Max]=current;
				KNNDistance[Max]=Dis;
				Max=getMax();
				MaxDis=KNNDistance[Max];
			}
		}
		if(MaxDis>DimenDis||KNum<K)
		{
			DeclineTraverse(current.lchild);
			DeclineTraverse(current.rchild);
		}
		SearchKNN(current.father);
	//	Node leftchild=current
	}
	public static void main(String[] args) throws IOException
	{
		args=new String[3];
		args[0]="knn.train";
		args[1]="knn.test";
		args[2]="knn.test.out";
		kdTree kd=new kdTree();
		kd.ReadDocument(args[0]);
	//	QuckSort(0,Value)
		root=new Node();
		kd.GenerateKdTree(0,Value.length-1,root);
		for(int i=0;i<Value.length;i++)
		{
			System.out.println(Value[i].point[0]+" "+Value[i].point[1]);
		}
		kd.Out(args[1], args[2]);
		System.out.println("Done");
	}
}


