
public class matrixGenerateThread extends Thread{

	int m,n;
	String tname;
	int[][] matrix;
	
	public void run() {
		matrix = generateMatrix(m,n);
//        System.out.println("\nMatrix Generated!");
    }

	public matrixGenerateThread(int a, int b, boolean isStart) {
		// TODO Auto-generated constructor stub
		m=a;
		n=b;
		if(isStart)
			start();
	}
	
	static int[][] generateMatrix(int m, int n){
    	int[][] matrix = new int [m] [n];
    	for (int i=0; i<m; i++) {
    	    for (int j=0; j<n; j++) {
    	        matrix[i][j] = (int) (Math.random()*10);
    	    }           
    	}
    return matrix;	
    }
	
	public int[][] getMatrix(){
		return matrix;
	}
}