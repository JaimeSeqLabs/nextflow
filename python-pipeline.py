
env = "python"
print(f'hello from {env}')

def foo():
    print("this is being executed inside a python function")

def callJava():
    print("calling a java method from python")
    javaMethod.call()

foo()
callJava()

# the answer to life
42