
public class MatrixMultiplicationThread extends Thread {
	
	/*
	 * =========Class Members================
	 * ======================================
	 * */
	int m1,n1, m2, n2;
	String tname;
	int y;
	App obj;
	int[][] matrix1;
    int[][] matrix2;
    int[] block_dimension;
    int size;
    boolean partialMethod = false;
    boolean atomicMethod  = false;
    
    
    /*
	 * ==============Class Methods==============
	 * =========================================
	 * */
    public MatrixMultiplicationThread(int start_m, int start_n, int end_m, int end_n, App o, String name) {
    	// Constructor for divide elements to thread method
		m1= start_m;
		n1= start_n;
		m2= end_m;
		n2= end_n;
		obj = o;
		tname=name;
		matrix1 = obj.getm1();
	    matrix2 = obj.getm2();
	    size = matrix1.length;
	}

	public MatrixMultiplicationThread(int start_m, int start_n, int[] block_dim, App o, String name) {
		// Constructor for partial multiplication on Locks thread method
		m1= start_m;
		n1= start_n;
		obj = o;
		tname=name;
		block_dimension = block_dim;
		partialMethod = true;
		matrix1 = obj.getm1();
	    matrix2 = obj.getm2();
	    size = matrix1.length;
	}
    
	public MatrixMultiplicationThread(int[] start_index, int[] block_dim, App o, String name) {
		// Constructor for partial multiplication on Atomic Array thread method
		m1= start_index[0];
		n1= start_index[1];
		obj = o;
		tname=name;
		block_dimension = block_dim;
		atomicMethod = true;
		matrix1 = obj.getm1();
	    matrix2 = obj.getm2();
	    size = matrix1.length;
	}

	public void run() {
        //  calculate value 
		if(partialMethod || atomicMethod) {
			partialmultiplication(m1, n1, block_dimension);
			return;
		}
		matrixMultiplication(m1, n1, m2, n2);
    }

	void matrixMultiplication(int m1, int n1, int m2, int n2) {
		while(m1 <= m2 && m1 < size) {
        	while( (n1 < size && m1 != m2) || (m1 == m2 && n1 <= n2 ) ) {
        		setvalue(m1,n1);
        		n1++;
        	}
        	n1 = 0;
        	m1++;
        }
	}
	
	void partialmultiplication(int start_index_m, int start_index_n, int[] block_dimension){
		int t1 = 0 , t2 = 0;
		while(t1 < block_dimension[0] ) {
			while(t2 < size) {
				setPartialValue(t1 + start_index_m, t2, block_dimension, start_index_m, start_index_n);
				t2++;
			}
			t2 = 0;
			t1++;
		}
	}
	
	void setPartialValue(int m, int n, int[] dimension, int start_index_m, int start_index_n) {
		int value = 0, temp = dimension[1];
		while(temp-- > 0) {
			value += matrix1[m][temp + start_index_n]*matrix2[temp + start_index_n][n];
		}
		if(partialMethod)
			obj.addResultToMatrixElement(m,n,value);
		else
			obj.addResultToMatrixAtomicElement(m,n,value);
//		System.out.format(" [%d] [%d]  =  %d  \t", m,n,value);
	}

	void setvalue(int m, int n){
		int value = 0, temp = size;
		while( temp-- > 0 ) {
			value += matrix1[m][temp]*matrix2[temp][n];
		}
		obj.setResultMatrixElement(m,n,value);
	}
}
