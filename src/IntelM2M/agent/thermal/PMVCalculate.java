package IntelM2M.agent.thermal;


import java.util.Calendar;

public class PMVCalculate {
	double m; // activity, W/m^2
	double w; // work of outer force, W/m^2
	double rh; // humidity ,%(0~100)
	double t_a; // average temperature, ¢XC
	double i_cl; // clothing, clo; 1 clo = 0.155 m^2 ¢XC/W
	double t_cl; // surface temperature of cloth, ¢XC
	double t_r; // average radial temperature¢X, C
	double v_ar; // average velocity of wind, m/s
	double f_cl;
	double h_c;
	double pa;
	double pmv;
	double ppd;

	/* default */
	public PMVCalculate(){
		java.util.Date date = new java.util.Date();
		m = 58; 
		w = 0;
		rh = 50;
		t_a = 26;
		
		int month = Calendar.getInstance().get(Calendar.MONTH);
		if(month >= 2 && month <= 3)
			i_cl = 0.7;
		else if(month >= 4 && month <= 7)
			i_cl = 0.5;
		else if(month >= 8 && month <= 9)
			i_cl = 1.0;
		else
			i_cl = 1.5;
		
		t_r = t_a;
		v_ar = 0.5;
		setFcl(i_cl); // set Fcl and convert Icl unit
		setInitialTclHc(30,30);
	}
	
	public PMVCalculate(double intensity,double temp, double vel){
		java.util.Date date = new java.util.Date();
		m = intensity; 
		w = 0;
		rh = 50;
		t_a = temp;
		int month = Calendar.getInstance().get(Calendar.MONTH);
		if(month >= 2 && month <= 3)
			i_cl = 0.7;
		else if(month >= 4 && month <= 7)
			i_cl = 0.5;
		else if(month >= 8 && month <= 9)
			i_cl = 1.0;
		else
			i_cl = 1.5;
		
		t_r = t_a;
		v_ar = vel;
		setFcl(i_cl); // set Fcl and convert Icl unit
		setInitialTclHc(30,30);
	}

	public PMVCalculate(double[] args){ // args = M, RH, Ta, Icl, Tr, Var
		m = args[0];
		rh = args[1];
		t_a = args[2];
		i_cl = args[3];
		t_r = args[4];
		v_ar = args[5];
		setFcl(i_cl); // set Fcl and convert Icl unit
		setInitialTclHc(30,30);
	}
	
	public PMVCalculate (double intensity, double temp, double vel, double humidity) {
		m = intensity; 
		w = 0;
		rh = humidity;
		t_a = temp;
		int month = Calendar.getInstance().get(Calendar.MONTH);
		if(month >= 2 && month <= 3)
			i_cl = 0.7;
		else if(month >= 4 && month <= 7)
			i_cl = 0.5;
		else if(month >= 8 && month <= 9)
			i_cl = 1.0;
		else
			i_cl = 1.5;
		
		t_r = t_a - 3;
		v_ar = vel;
		setFcl(i_cl); // set Fcl and convert Icl unit
		setInitialTclHc(30,30);
	}
	
	public void setInitialTclHc(double tcl, double hc) {
		t_cl = tcl;
		h_c = hc;
	}
	
	public void setM(double a) {	// set Activity factor
		m = a;
	}

	public void setTa(double t) {
		t_a = t;
	}

	public void setRh(double h) {
		rh = h;
	}
	
	public void setIcl(double icl) {
		i_cl = icl; // unit : clo
		setFcl(i_cl); // set Fcl and convert Icl unit
	}

	public void setTr(double tr) {
		t_r = tr;
	}

	public void setVar(double var) {
		v_ar = var;
	}

	public double[] getPMVandPPD() {
		double[] PMV_PPD = new double[2];
		double[] q = {t_cl,h_c};

		setPa();
		findMinVec(q);
		// calculate PMV
		PMV_PPD[0] = (0.303 * Math.exp(-0.036 * m) + 0.028);
		PMV_PPD[0] *= m - (3.05e-3 * (5733 - 6.99 * m - pa)) -
			0.42 * (m - 58.15) - 1.7e-5 * m * (5867 - pa) -
			0.0014 * m * (34 - t_a) - 3.96 * Math.pow(10, -8) * f_cl *
			(Math.pow(t_cl + 273, 4) - Math.pow(t_r + 273, 4)) -
			f_cl * h_c * (t_cl - t_a);

		// calculate PPD
		PMV_PPD[1] = 100 - 95 * Math.exp(-0.03353 * Math.pow(PMV_PPD[0],4) - 0.2179 * Math.pow(PMV_PPD[0], 2));
		pmv = PMV_PPD[0];
		ppd = PMV_PPD[1];
		return PMV_PPD;
	}

	/* to get the t_a which can make the consequent pmv = 0 
	 * use 1% to gradually approach target value 
	 * (experimental result with quite good speed and accuracy) */
	public synchronized double getMostFitTemp(double targetPMV) {
		if(Math.abs(pmv - targetPMV) < 0.001) {
			return t_a;
		}
		if(pmv > targetPMV) {
			t_a *= 0.99;
			getPMVandPPD();
			getMostFitTemp(targetPMV);
		}
		else {
			t_a *= 1.01;
			getPMVandPPD();
			getMostFitTemp(targetPMV);
		}
		return t_a;
	}	

	/* RH = relative humidity , Ta = temp */
	public void setPa() {
		pa = rh * 10 * Math.exp(16.6536 - (4030.183 / (t_a + 235)));
	}

	public void setFcl(double icl) {
		icl *= 0.155;
		if(icl <= 0.078)
			f_cl = 1 + 1.29 * icl;
		else
			f_cl = 1.05 + 0.645 * icl;
		i_cl = icl;
	}

	public double pmvEQF(double q[]) { // q[0] = Tcl, q[1] = Hc
		double q1,q2;

		/*q1=tcl*/
		q1 = 35.7 - 0.028 * m - i_cl * (3.96e-8 * f_cl * (Math.pow(q[0] + 273, 4) - Math.pow(t_r + 273, 4))+ f_cl * q[1] * (q[0] - t_a)) - q[0];
		//System.out.println(q[0]+ " " + q[1] + " " + "q1 = " + q1);
		/*q2=hc*/
		if ( 2.38*Math.pow(q[0]-t_a, 0.25) >= 12.1*Math.pow(v_ar, 0.5) )
			q2 = 2.38 * Math.pow(q[0]-t_a, 0.25) - q[1];
		else
			q2 = 12.1 * Math.pow(v_ar, 0.5) - q[1];
		//System.out.println("q2 = " + q2);
		return q1 * q1 + q2 * q2;
	}

	/* x0 = [Tcl, Hc] */
	public void findMinVec(double[] x0) {
		double[] x1 = new double[2];
		double[] x2 = new double[2];
		double[] m = new double[2];
		double[] r = new double[2];
		double[] swap = new double[2];
		double[][] xList = new double[3][2];
		double f_of_r;
		x1[0] = x0[0] * 1.05;
		x1[1] = x0[1];
		x2[0] = x0[0];
		x2[1] = x0[1] * 1.05;
		xList[0] = x0;
		xList[1] = x1;
		xList[2] = x2;

		while(true) {
			/*System.out.println("before : " + xList[0][0] + " " + xList[0][1] + " => " + pmvEQF(xList[0]) + " ;; "  
			  + xList[1][0] + " " + xList[1][1] + " => " + pmvEQF(xList[1]) + " ;; "  
			  + xList[2][0] + " " + xList[2][1] + " => " + pmvEQF(xList[2]) );*/
			for(int i = 0; i < 2; i++) {
				for(int j = 0; j < 2 - i ; j++) {
					if(pmvEQF(xList[j]) - pmvEQF(xList[j+1]) > 0) {
						swap = xList[j];
						xList[j] = xList[j+1];
						xList[j+1] = swap;
					}
				}
			}
			m[0] = (xList[0][0] + xList[1][0]) / 2;
			m[1] = (xList[0][1] + xList[1][1]) / 2;
			/*System.out.println("check : " + xList[0][0] + " " + xList[0][1] + " => " + pmvEQF(xList[0]) + " ;; "  
			  + xList[1][0] + " " + xList[1][1] + " => " + pmvEQF(xList[1]) + " ;; "  
			  + xList[2][0] + " " + xList[2][1] + " => " + pmvEQF(xList[2]) );
			  */
			r[0] = 2*m[0] - xList[2][0];
			r[1] = 2*m[1] - xList[2][1];
			//System.out.println("r : " + r[0] + " " + r[1]);
			f_of_r = pmvEQF(r);
			if(pmvEQF(xList[0]) <= f_of_r && f_of_r < pmvEQF(xList[1])) {
				xList[2][0] = r[0];
				xList[2][1] = r[1];
				//System.out.println("accept at 1");
				continue;
			}
			else if(f_of_r < pmvEQF(xList[0])) {
				double[] s = new double[2];
				double f_of_s;
				s[0] = m[0] + 2*(m[0] - xList[2][0]);
				s[1] = m[1] + 2*(m[1] - xList[2][1]);
				f_of_s = pmvEQF(s);
				if(f_of_s < f_of_r) {
					xList[2][0] = s[0];
					xList[2][1] = s[1];
					//System.out.println("accept at 2-1");
					continue;
				}
				else {
					xList[2][0] = r[0];
					xList[2][1] = r[1];
					//System.out.println("accept at 2-2");
					continue;
				}
			}
			else {
				double[] c = new double[2];
				double f_of_c;
				if(f_of_r < pmvEQF(xList[2])) {
					c[0] = m[0] + (r[0] - m[0]) / 2;
					c[1] = m[1] + (r[1] - m[1]) / 2;
					f_of_c = pmvEQF(c);
					if(f_of_c < f_of_r) {
						xList[2][0] = c[0];
						xList[2][1] = c[1];
						//System.out.println("accept at 3-1");
						continue;
					}
				}
				else {
					c[0] = m[0] + (xList[2][0] - m[0]) / 2;
					c[1] = m[1] + (xList[2][1] - m[1]) / 2;
					f_of_c = pmvEQF(c);
					if(f_of_c < pmvEQF(xList[2])) {
						xList[2][0] = c[0];
						xList[2][1] = c[1];
						//System.out.println("accept at 3-2");
						continue;
					}
				}
			}
			/*
			   Calculate the n points
			   v(i) = x(1) + (x(i) ¡V x(1))/2
			   and calculate f(v(i)), i = 2,...,n+1. 
			   The simplex at the next iteration is x(1), v(2),...,v(n+1). Shrink
			   */

			x1[0] = xList[0][0] + (xList[1][0] - xList[0][0]) / 2;
			x1[1] = xList[0][1] + (xList[1][1] - xList[0][1]) / 2;
			x2[0] = xList[0][0] + (xList[2][0] - xList[0][0]) / 2;
			x2[1] = xList[0][1] + (xList[2][1] - xList[0][1]) / 2;
			if(x1[0] == xList[1][0] && x1[1] == xList[1][1] && 
					x2[0] == xList[2][0] && x2[1] == xList[2][1]    ) {
				t_cl = xList[0][0];
				h_c = xList[0][1];
				break;
					}
			else {
				xList[1][0] = x1[0]; xList[1][1] = x1[1];
				xList[2][0] = x2[0]; xList[2][1] = x2[1];
			}
		}
	}
}


