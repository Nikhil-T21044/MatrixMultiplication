import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class App {    // Main Application Class
	private int[][] m1, m2, result_matrix, result_matrix_sync;
	private ReentrantLock matrix_lock = new ReentrantLock();
	private AtomicInteger[][] atomic_result_matrix;
    
	public int[][] getm1(){		return m1;	}
	public int[][] getm2(){		return m2;	}
	public int[][] getResultMatrix(){		return result_matrix;	}
	public void setResultMatrixElement(int m, int n, int value) {	result_matrix[m][n] = value;}
	public void addResultToMatrixAtomicElement(int m, int n, int value) { atomic_result_matrix[m][n].addAndGet(value); }
	public void addResultToMatrixElement(int m, int n, int value) {
		try {
			matrix_lock.lock();
			result_matrix_sync[m][n] += value; 
		}finally {
			matrix_lock.unlock();
		}
		}
	
    public static void main(String args[]) {
    	System.out.println("Hello Java"); 
    	int loop = 10;
    	
    	int n = 128 ;
    	
    	while(loop-- > 0) {  // no of cases 
    		App instance = new App();
    		n += 128; // incremental matrix size for cases
    		instance.generateInput(n);
    		
    		System.out.format("\n ============ Same matrix(" + n + ","+ n + ") with no of runs ============ \n");
    		System.out.format("(Dimension: "+ n + " )\tDivide & Assign\t\tPartial Lock Based\tPartial Atomic Array Based ");
    		
    		int inner_loop = 3;
    		while(inner_loop-- > 0) {
    			
    		/*
    		 * ============= Matrix Multiplication By dividing/assigning no of elements  to compute to each thread =====
    		 * 
    		 * ====> Synchronization ->  No need every thread processing the value of exclusive elements assign to them
    		 * */	
    		long start = System.currentTimeMillis();
    		instance.multiplyMatrixByDividingElementsToThreads();
    		long end = System.currentTimeMillis();
    		System.out.format("\nexeccution time :\t"+ (end - start) + " ms \t\t");
    		
    		
    		/*
    		 * =========== Matrix Multiplication by Dividing matrix to sub matrices and doing partial calculation in each thread ======
    		 * 
    		 * ====> Synchronization ->  Using Lock Based Method (acquiring lock before updating value)
    		 * */
    		start = System.currentTimeMillis();
    		instance.multiplyMatrixByPartialCalculationOnBlocks();
    		end = System.currentTimeMillis();
    		System.out.format("|\t"+(end - start) + " ms\t\t");
    		
    		
    		/*
    		 * =========== Matrix Multiplication by Dividing matrix to sub matrices and doing partial calculation in each thread ======
    		 * 
    		 * ====> Synchronization ->  Using Atomic Integer Array (add and set on atomic level) , So no need of synchronizing
    		 * */
    		start = System.currentTimeMillis();
    		instance.multiplyMatrixByPartialCalculationOnAtomicBlocks();
    		end = System.currentTimeMillis();
    		System.out.format("|\t"+(end - start) + " ms");
    		
    		
    		}
    		System.out.println();
    		instance.printOutput(); // print only when matrix less than or equal   (10 X 10)
        }

    }

    void generateInput(int n) { // Input Generation Module, can add another method also like getting input from file or console etc.
    	matrixGenerateThread mt1 = new matrixGenerateThread(n, n, true);
    	matrixGenerateThread mt2 = new matrixGenerateThread(n, n, true);
    	
    	try {
			mt1.join();
			mt2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	m1 = mt1.getMatrix();
    	m2 = mt2.getMatrix();      
    }
    
    void printOutput() {
    	if(m1.length > 10)  return;
    	System.out.println("=======================");
    	printMatrix(m1);
    	System.out.println("=======================");
    	printMatrix(m2);
    	System.out.println("==============Element assigned to Thread Method==========");
    	printMatrix(result_matrix);
    	System.out.println("==============Partial calculation By block Division==========");
    	printMatrix(result_matrix_sync);
    	System.out.println("==============Partial calculation On Atomic By block Division==========");
    	printMatrix(atomic_result_matrix);
    }
    
	void multiplyMatrixByDividingElementsToThreads() {
    	result_matrix = new int [m1.length] [m2.length];
    	MatrixMultiplicationThread t1, t2, t3, t4;
    	
    	/*====================================  Dividing Job Strategy =================================
  	  =============================================================================================*/
    	
    	int matrix_size = m1[0].length * m1.length ;
    	
    	int thread_block_size = matrix_size/4;
    	
    	int[] block1 = findingLastElementIndex(m1.length,m1.length,0,0,thread_block_size);
    	int[] block2 = findingLastElementIndex(m1.length,m1.length,block1[2],block1[3],thread_block_size);
    	int[] block3 = findingLastElementIndex(m1.length,m1.length,block2[2],block2[3],thread_block_size);
//    	int[] block4 = findingLastElementIndex(m1.length,m1.length,block3[2],block3[3],thread_block_size);

//    	extendedThread t1 = new extendedThread(		   0, 		  0, n-1,n-1, this,"first");
    	 t1 = new MatrixMultiplicationThread(		   0, 		  0, block1[0],block1[1], this,"first");
    	 t2 = new MatrixMultiplicationThread(block1[2], block1[3], block2[0],block2[1], this, "second");
    	 t3 = new MatrixMultiplicationThread(block2[2], block2[3], block3[0],block3[1], this, "third");
//    	 t4 = new extendedThread(block3[2], block3[3], block4[0],block4[1], this, "fourth");
    	 t4 = new MatrixMultiplicationThread(block3[2], block3[3], m1.length-1,m1.length-1, this, "fourth");
    	 
     	/*====================================  starting and waiting for job to finish ==================
   	  	=================================================================================================*/
    	 
    	 t1.start();
         t2.start();
         t3.start();
         t4.start();
         
         try {
 			t1.join();
 			t2.join();
 			t3.join();
 			t4.join();
 		} catch (InterruptedException e) {
 			 System.out.println("Exception occured at runnning thread");
 			e.printStackTrace();
 		}
    }

    void multiplyMatrixByPartialCalculationOnBlocks() {
    	result_matrix_sync = new int [m1.length] [m2.length];
    	MatrixMultiplicationThread t1, t2, t3, t4;
    	
    	/*====================================  Dividing Job Strategy =================================
  	  =============================================================================================*/
    	
    	int block_size_m = m1.length/2;
    	int block_size_n = m1[0].length/2;
		
		 t1 = new MatrixMultiplicationThread(		   	0, 		  	 	0,		new int[] {block_size_m , block_size_n}, 								this,"first");
    	 t2 = new MatrixMultiplicationThread(		   	0,	 block_size_n,		new int[] {block_size_m , m1[0].length - block_size_n}, 				this, "second");
    	 t3 = new MatrixMultiplicationThread(block_size_m, 				0, 		new int[] {m1.length - block_size_m , block_size_n}, 					this, "third");
    	 t4 = new MatrixMultiplicationThread(block_size_m, 	 block_size_n, 		new int[] {m1.length - block_size_m , m1[0].length - block_size_n},		this, "fourth");
    	 
     	/*====================================  starting and waiting for job to finish ==================
   	  	=================================================================================================*/
    	 
    	 t1.start();
         t2.start();
         t3.start();
         t4.start();
         
         try {
 			t1.join();
 			t2.join();
 			t3.join();
 			t4.join();
 		} catch (InterruptedException e) {
 			 System.out.println("Exception occured at runnning thread");
 			e.printStackTrace();
 		}
    }

    void multiplyMatrixByPartialCalculationOnAtomicBlocks() {
    	
    	atomic_result_matrix = new AtomicInteger[m1.length][m2.length]; //declaring size
    	for (int i = 0; i < m1.length; i++) { //initializing
    	    for (int j = 0; j < m2.length; j++) {
    	    	atomic_result_matrix[i][j] = new AtomicInteger();
    	    }
    	}
    	MatrixMultiplicationThread t1, t2, t3, t4;
    	
    	/*====================================  Dividing Job Strategy =================================
  	  =============================================================================================*/
    	
    	int block_size_m = m1.length/2;
    	int block_size_n = m1[0].length/2;
		
		 t1 = new MatrixMultiplicationThread(		   	new int[] {0, 		  	 	0},		new int[] {block_size_m , block_size_n}, 								this,"first");
    	 t2 = new MatrixMultiplicationThread(		   	new int[] {0,	 block_size_n},		new int[] {block_size_m , m1[0].length - block_size_n}, 				this, "second");
    	 t3 = new MatrixMultiplicationThread(new int[] {block_size_m, 				0},		new int[] {m1.length - block_size_m , block_size_n}, 					this, "third");
    	 t4 = new MatrixMultiplicationThread(new int[] {block_size_m, 	 block_size_n},		new int[] {m1.length - block_size_m , m1[0].length - block_size_n},		this, "fourth");
    	 
     	/*====================================  starting and waiting for job to finish ==================
   	  	=================================================================================================*/
    	 
    	 t1.start();
         t2.start();
         t3.start();
         t4.start();
         
         try {
 			t1.join();
 			t2.join();
 			t3.join();
 			t4.join();
 		} catch (InterruptedException e) {
 			 System.out.println("Exception occured at runnning thread");
 			e.printStackTrace();
 		}
    }

/*    int[][] generateMatrix(int m, int n){
    	int[][] matrix = new int [m] [n];
    	for (int i=0; i<m; i++) {
    	    for (int j=0; j<n; j++) {
    	        matrix[i][j] = (int) (Math.random()*10);
    	    }           
    	}
    return matrix;	
    }*/
    
    void printMatrix(int[][] matrix) {
    	for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {

                System.out.printf("%d\t", matrix [i][j]);
            }           
            System.out.println();
        }
    }

    void printMatrix(AtomicInteger[][] matrix) {
    	for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {

                System.out.printf("%d\t", matrix[i][j].get());
            }           
            System.out.println();
        }
    }

    int[] findingLastElementIndex(int m, int n, int start_index_m, int start_index_n, int block_length ){
    	///ROW MAJOR METHOD
    	int a = start_index_m, b = start_index_n;
    	int next_index_m, next_index_n;
    	int x = 0;
    	while(x++ < block_length-1) {
    		b++;
    		if(b == n) { 
    			a++;
    			b = 0;
    		}
    	}
    	next_index_n = b+1;
    	if(next_index_n == n) {
    		next_index_m = a+1;
    		next_index_n = 0;
		}else {
			next_index_m = a;
		}

    	return new int[] {a, b, next_index_m, next_index_n};
    }
}