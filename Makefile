all: subblocks

subblocks:
	cd user; make
	cd ecp; make
	cd tes; make
 
clean:
	rm -f user/*.class
	rm -f ecp/*.class
	rm -f tes/*.class
