
CMD = ant

all: apk

apk:
	@$(CMD) debug

release:
	@$(CMD) release

clean:
	@$(CMD) clean

.PHONY: icon

icon:
	@$(CMD) icon
