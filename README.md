# java-bert-predict
1.Java调用简单的tensorflow模型
2.Java调用bert模型
3.Java调用基于bert的复杂模型

# usage

1. download google bert pretrain from [here](https://storage.googleapis.com/bert_models/2018_11_03/multilingual_L-12_H-768_A-12.zip) and unzip it into a proper path.

2. because java cannot work with tensorflow checkpoint directly, we need to transform checkpoint into saved_model with following command 
	> python script/checkpoint_to_saved_model.py path/to/unzipped/pretrain/checkpoint path/to/save/model

1. run java program with path/to/save/model as params


