package de.tub.citydb.db.kmlExporter;

public class TexCoords {

		private double s;
		private double t;
		
		public TexCoords(double s, double t) {
			this.setS(s);
			this.setT(t);
		}

		protected void setS(double s) {
			this.s = s;
		}

		protected double getS() {
			return s;
		}

		protected void setT(double t) {
			this.t = t;
		}

		protected double getT() {
			return t;
		}
}
