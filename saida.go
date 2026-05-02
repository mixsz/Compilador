package main

func main(){
	var x int = 1
	var y int = 2
	var z int = 99
	var a string = "afafawfds"
	if(x<10){
		if(y>1 && z!=99){
			x = 99
		}else if(y==0){
			x = 0
		}else{
			x++
			a = "oi"
		}
	}else if(x==5){
		z = 3
	}else{
		y = 0
	}
}
