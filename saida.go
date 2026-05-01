package main

func main(){
	var x int = 1
	var y int = 2
	var z int = 99
	if(x<10){
		if(y>1 && z!=99){
			x = 99
		}else if(y==0){
			x = 0
		}else{
			x = 1
		}
	}else if(x==5){
		var z int = 3
	}else{
		y = 0
	}
}
