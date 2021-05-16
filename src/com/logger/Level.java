package com.logger;

public enum Level {

	NONE {
		@Override
		String getColor() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	INFO{
		@Override
		String getColor() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	WARNING{

		@Override
		String getColor() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	ERROR{

		@Override
		String getColor() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	DEBUG{
		@Override
		String getColor() {
			// TODO Auto-generated method stub
			return "";
		}
	};
	
	
	abstract String getColor();
}
